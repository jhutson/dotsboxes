# Dots and Boxes Game (Backend)

[![Python CI](https://github.com/jhutson/dotsboxes/actions/workflows/python-app.yml/badge.svg)](https://github.com/jhutson/dotsboxes/actions/workflows/python-app.yml)

This project is the (in-progress) backend for a version of the [Dots and Boxes game](https://en.wikipedia.org/wiki/Dots_and_Boxes). It is built as the implementation of an AWS Lambda function.

## Getting Started
This project requires [Python 3.9](https://www.python.org/downloads/release/python-390/). Once you've cloned the project from git, run the following from the root folder to set up a virtual environment and install required packages:

```
python3.9 -m venv --upgrade-deps venv && source venv/bin/activate && pip install -r requirements.txt
```

### Tests
Once setup is done, run `tox` to execute tests. 

### Iterative Development
Run `pip install -e .` to install the dotsboxes module in development mode. This allows for running `pytest` directly for unit tests.

## Packaging
Run `./make_package` to build a zip file used as the source of an AWS Lambda function. It is placed in `dist/lambda-dotsboxes.zip`.

**TODO:** Numpy is bundled with the rest of the code in the zip. This makes the file larger than it needs to be for incremental changes. This should be broken up into one zip with numpy, and another for the project code.  The numpy zip can be deployed as a lambda layer, keeping the project zip small.
