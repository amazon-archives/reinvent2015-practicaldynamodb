## This project is used for demonstrating how Amazon DynamoDB could be used together with AWS Lambda to perform real-time and batch analysis of domain specific data. Real-time analysis is done using DynamDB streams as an event source of a Lambda function. Batch processing utilizes the parallel scan Action of DynamoDB to distribute work to Lambda. Although this is a Maven project, AWS Lambda functions cannot be deployed by Maven. It is expected to use Eclipse to deploy the AWS Lambda functions and run the sample code.

#### Prerequisite
* [Install Eclipse to your computer](https://wiki.eclipse.org/Eclipse/Installation)
* [Install AWS Toolkit for Eclipse](https://aws.amazon.com/eclipse/)
* [Install Eclipse Maven plugin](http://www.eclipse.org/m2e/)
* Use git clone https://github.com/awslabs/reinvent2015-practicaldynamodb.git to download this folder to your local computer, import to your Eclipse IDE environment as a Maven project.

#### STEP 1 - Prepare demo resources
* Setup Credentials
  * Inside the datasetinit package, change Constants.LOCAL_CRED_PROFILE_NAME to the desired profile name. [Providing AWS Credentials in the AWS SDK for Java](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html)
* Create DynamoDB tables
  * In datasetinit package, run the CreateFunctionTrackerTable, CreateHighScoresByDateTable, CreatePlayerStatsTable and CreateScoresTable classes.
* Create a DynamoDB Streams sourced Lambda function
    * Upload the streamhandling.ScoresTableTrigger Lambda function to the us-west-2 region(use any function name).
      - Right click handleRequest method -> AWS Lambda -> Upload Function to AWS Lambda...
    * In the console, add a new event source for this Lambda function
      - DynamoDB table: Scores
      - Batch size: 100 (default)
      - Starting position: trim horizon
* Create the Batch processing Lambda function
    * Upload the parallelscan.SegmentScannerFunctionHandler Lambda function to the us-west-2 region.
      - Function name: TableSegmentScannerFunction
      - Timeout: 300s (max)

#### STEP 2 - Start generating scores data
* Right click handleRequest method -> AWS Lambda -> Run Function on AWS Lambda...
	* An insertion event will be simulated using dynamodb-event.insert.json file to insert a record
    * You will notice a new record is inserted to PlayerStats table from AWS console
* Run datasetinit.GenerateScores to simulate inserting records to the Scores table.
* From the console, you will notice the PlayerStats table updating records due to the streaming Lambda function setup earlier.

#### STEP 3 - Invoke scanner function to transform existing data
* Run the parallelscan.FunctionInvoker class.
* After it finishes, check HighScoresByDate table.

For more information, refer to [reInvent 2015](https://www.youtube.com/watch?v=XByPxb_VvpY)
