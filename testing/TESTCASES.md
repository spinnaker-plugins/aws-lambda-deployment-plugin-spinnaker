## Function Test Cases

### Scenario 1
Test creation of a simple AWS Lambda function
| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 1   | Create Lambda with AWS Lambda Deployment Stage | A function is created with function name {APP_NAME}-test-functional-scenario1 | 

### Scenario 2

| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 2   | Create Lambda with AWS Lambda Deployment Stage | A function is created with function name {APP_NAME}-test-functional-scenario2   | 
| 2   | Edit Lambda description, memory, and tags | The function description, memory, and tags are updated | 

### Scenario 3
| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 3   | Create Lambda with AWS Lambda Deployment Stage  | A function is created with function name {APP_NAME}-test-functional-scenario3 | 
| 3   | Add an event trigger to the Stage | The event trigger is now added to the function | 

### Scenario 4
| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 4   | Create Lambda with AWS Lambda Deployment Stage  | A function is created with function name {APP_NAME}-test-functional-scenario4 | 
| 4   | Delete Function with AWS Lambda Destroy Stage | The Lambda will be completely deleted | 

### Scenario 5
Testing increasing the function memory size
| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 5   | Create Lambda with AWS Lambda Deployment Stage | A function is created with function name {APP_NAME}-test-functional-scenario5 | 
| 5   | Edit Lambda memory size | The lambda memory size will now be 1024 | 

### Scenario 6
| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 6   | Create Lambda with AWS Lambda Deployment Stage | A function is created with function name {APP_NAME}-test-functional-scenario6 | 
| 6   | Delete Function with AWS Lambda Destroy Stage using SpEL | The Lambda will be completely deleted | 

### Scenario 7
| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 7   | Create Lambda with AWS Lambda Deployment Stage | A function is created with function name {APP_NAME}-test-functional-scenario7 | 
| 7   | Delete Function Version with AWS Lambda Destroy Stage | The function newest version is deleted | 
| 7   | Delete Function Version with AWS Lambda Destroy Stage | The stage will succeed and no changes will be made to the function | 

### Scenario 8
| Scenario Id | Action | Expected Result | 
| ----------- | ----------- | ----------- |
| 8   | Create Lambda with AWS Lambda Deployment Stage | A function is created with function name {APP_NAME}-test-functional-scenario7 | 
| 8   | Delete Function Version with AWS Lambda Destroy Stage | The stage will fail with message `No version found for Lambda function. Unable to perform delete operation` | 




