#!/bin/bash

echo "Running tests..."
mvn test
RESULT=$?

if [ $RESULT -ne 0 ]; then
  echo "Tests failed."
  exit 1
else
  echo "Tests passed successfully."
  exit 0
fi

