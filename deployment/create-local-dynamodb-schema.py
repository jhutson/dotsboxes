import boto3
import yaml
from argparse import ArgumentParser
from botocore.exceptions import ClientError
from datetime import datetime
from os import chmod, makedirs
from pathlib import Path
from random import randint

ENDPOINT_URL = 'http://localhost:9000'


class TableNames:

  def __init__(self, env_file_path: Path) -> None:
    self.env_table_names = {
      'HDEV_TABLE_GAME_SESSIONS': '',
      'HDEV_TABLE_PLAYERS': ''
    }

    self.table_names_to_env = {
      'dotsboxesGameSessions': 'HDEV_TABLE_GAME_SESSIONS',
      'dotsboxesPlayers': 'HDEV_TABLE_PLAYERS'
    }

    self.output_name_to_env = {
      'GamesSessionsTableName': 'HDEV_TABLE_GAME_SESSIONS',
      'PlayersTableName': 'HDEV_TABLE_PLAYERS'
    }

    if env_file_path.exists and env_file_path.is_file():
      with open(env_file_path, 'r') as file:
        for line in file:
          pair = line.rstrip().split('=')
          if len(pair) == 2 and pair[0] in self.env_table_names:
            table_name = pair[1].lstrip("'").rstrip("'")
            self.env_table_names[pair[0]] = table_name

  def write_env_variables(self, env_file_path: Path, outputs):
    with open(env_file_path, 'w') as file:
      for output_name in self.output_name_to_env:
        variable_name = self.output_name_to_env[output_name]
        table_name = outputs.get(output_name, 'missing')

        file.writelines((
          f'{variable_name}=\'{table_name}\'\n',
          f'export {variable_name}\n',
          '\n',
        ))

  def _get_unique_resource_name(self, base_prefix: str, base_name: str) -> str:
    now = datetime.now()
    timestamp = now.strftime('%Y%m%d%H%M')

    return f'{base_prefix}-{base_name}-{timestamp}-{randint(1000,10000)}'

  def get_resource_name(self, base_prefix: str, base_name: str) -> str:
    if base_name in self.table_names_to_env:
      existing_name = self.env_table_names[self.table_names_to_env[base_name]]
      if existing_name:
        return existing_name

    return self._get_unique_resource_name(base_prefix, base_name)


class DynamoClient:

  def __init__(self) -> None:
     self.dynamodb = boto3.resource('dynamodb', endpoint_url = ENDPOINT_URL)

  def delete_table(self, table_name: str):
      print(f'Deleting table "{table_name}"')
      table = self.dynamodb.Table(table_name)
      table.delete()

  def table_exists(self, table_name):
    try:
      table = self.dynamodb.Table(table_name)
      table.load()
      return True
    except ClientError as error:
      if error.response['Error']['Code'] == 'ResourceNotFoundException':
        return False
      else:
        raise

  def create_table(self, table_name: str, resource, delete_if_exists=True):
    if delete_if_exists and self.table_exists(table_name):
      self.delete_table(table_name)

    print(f'Creating table "{table_name}"')
    parameters = {'TableName': table_name, **resource['Properties'] }
    print(parameters)

    table = self.dynamodb.create_table(**parameters)
    table.wait_until_exists()


class Ref(yaml.YAMLObject):
    yaml_loader = yaml.SafeLoader
    yaml_tag = '!Ref'
    def __init__(self, val):
        self.val = val

    @classmethod
    def from_yaml(cls, loader, node):
        return cls(node.value)


def main():
  parser = ArgumentParser(description="Deploy DynamoDB tables to local instance.")
  # parser.add_argument('-e', '--environment', required=True, choices=['dev', 'test'], help="Name of environment to use. 'dev' is for local development. 'test' is for unit testing and continuous integration.")
  arguments = parser.parse_args()

  # environment = arguments.environment
  environment = 'dev'
  base_folder = Path(environment)
  local_env_path = (base_folder / 'env').resolve()

  table_names = TableNames(local_env_path)
  client = DynamoClient()

  with open('dotsboxes-dynamodb.yaml', 'r') as yamlFile:
    documents = yaml.safe_load_all(yamlFile)
    if not documents:
      return

    resolved_outputs = dict()
    references = dict()

    for document in documents:
      resources = document.get('Resources', None)
      if resources:
        for resource_name in resources:
          if resource_name in references:
            raise f'Resource name aleady defined: "{resource_name}"'

          resource = resources[resource_name]

          if resource.get('Type') == 'AWS::DynamoDB::Table':
            unique_resource_name = table_names.get_resource_name(environment, resource_name)
            references[resource_name] = unique_resource_name
            client.create_table(unique_resource_name, resource)

      outputs = document.get('Outputs')
      if outputs:
        for output_name in outputs:
          if output_name in resolved_outputs:
            raise f'Output name aleady defined: "{output_name}"'

          output_ref = outputs[output_name]['Value'].val
          resolved_outputs[output_name] = references[output_ref]

    makedirs(base_folder, exist_ok=True)
    table_names.write_env_variables(local_env_path, resolved_outputs)

if __name__ == '__main__':
  main()