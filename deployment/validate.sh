[ -d deployment ] && cd deployment

aws cloudformation validate-template --template-body file://dotsboxes-dynamodb.yaml