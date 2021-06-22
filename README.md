# AWS CloudSearch Monitoring Extension

## Use Case
Captures CloudSearch statistics from Amazon CloudWatch and displays them in the AppDynamics Metric Browser.

**Note : By default, the Machine agent can only send a fixed number of metrics to the controller. This extension potentially reports thousands of metrics, so to change this limit, please follow the instructions mentioned [here](https://docs.appdynamics.com/display/PRO40/Metrics+Limits).**

## Installation

1. Run 'mvn clean install' from aws-cloudsearch-monitoring-extension
2. Copy and unzip AWSCloudSearchMonitor-\<version\>.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yaml file in AWSCloudSearchMonitor and provide the required configuration (see Configuration section)
4. Restart the Machine Agent.

## Configuration

### config.yaml

**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

| Section | Fields | Description | Example |
| ----- | ----- | ----- | ----- |
| **accounts** | | Fields under this section can be repeated for multiple accounts config |  |
| | awsAccessKey | AWS Access Key |  |
| | awsSecretKey | AWS Secret Key |  |
| | displayAccountName | Display name used in metric path | "MyAppD" |
| | regions | Regions where CloudsSearch is registered | **Allowed values:**<br/>"ap-southeast-1",<br/>"ap-southeast-2",<br/>"ap-northeast-1",<br/>"eu-central-1",<br/>"eu-west-1",<br/>"us-east-1",<br/>"us-west-1",<br/>"us-west-2",<br/>"sa-east-1" |
| **credentialsDecryptionConfig** | ----- | ----- | ----- |
| | enableDecryption | If set to "true", then all aws credentials provided (access key and secret key) will be decrypted - see AWS Credentials Encryption section |  |
| | decryptionKey | The key used when encypting the credentials |  |
| **proxyConfig** | ----- | ----- | ----- |
| | host | The proxy host (must also specify port) |  |
| | port | The proxy port (must also specify host) |  |
| | username | The proxy username (optional)  |  |
| | password | The proxy password (optional)  |  |
| **metricsConfig** | ----- | ----- | ----- |
| metricTypes | | Fields under this section can be repeated for multiple metric types override |  |
| | metricName | The metric name | "SuccessfulRequests" |
| | statType | The statistic type | **Allowed values:**<br/>"ave"<br/>"max"<br/>"min"<br/>"sum"<br/>"samplecount" |
| | ----- | ----- | ----- |
| | excludeMetrics | Metrics to exclude - supports regex | "CPUUtilization",<br/>"Swap.*" |
| metricsTimeRange |  |  |  |
| | startTimeInMinsBeforeNow | The no of mins to deduct from current time for start time of query | 5 |
| | endTimeInMinsBeforeNow | The no of mins to deduct from current time for end time of query.<br>Note, this must be less than startTimeInMinsBeforeNow | 0 |
| | ----- | ----- | ----- |
| | maxErrorRetrySize | The max number of retry attempts for failed retryable requests | 1 |
| **concurrencyConfig** |  |  |  |
| | noOfAccountThreads | The no of threads to process multiple accounts concurrently | 3 |
| | noOfRegionThreadsPerAccount | The no of threads to process multiple regions per account concurrently | 3 |
| | noOfMetricThreadsPerRegion | The no of threads to process multiple metrics per region concurrently | 3 |
| | ----- | ----- | ----- |
| | metricPrefix | The path prefix for viewing metrics in the metric browser. | "Custom Metrics\|Amazon Route53\|" |


**Below is an example config for monitoring multiple accounts and regions:**

~~~
accounts:
  - awsAccessKey: "XXXXXXXX1"
    awsSecretKey: "XXXXXXXXXX1"
    displayAccountName: "TestAccount_1"
    regions: ["us-east-1","us-west-1","us-west-2"]

  - awsAccessKey: "XXXXXXXX2"
    awsSecretKey: "XXXXXXXXXX2"
    displayAccountName: "TestAccount_2"
    regions: ["eu-central-1","eu-west-1"]

credentialsDecryptionConfig:
    enableDecryption: "false"
    decryptionKey:

proxyConfig:
    host:
    port:
    username:
    password:    

metricsConfig:
    metricTypes:
      - metricName: "CurrItems"
        statType: "max"

      - metricName: "DecrHits"
        statType: "sum"        

    excludeMetrics: ["DeleteMisses", "Get.*"]

    metricsTimeRange:
      startTimeInMinsBeforeNow: 5
      endTimeInMinsBeforeNow: 0

    maxErrorRetrySize: 0

concurrencyConfig:
  noOfAccountThreads: 3
  noOfRegionThreadsPerAccount: 3
  noOfMetricThreadsPerRegion: 3

metricPrefix: "Custom Metrics|Amazon Route53|"
~~~

### AWS Credentials Encryption
To set an encrypted awsAccessKey and awsSecretKey in config.yaml, follow the steps below:

1. Download the util jar to encrypt the AWS Credentials from [here](https://github.com/Appdynamics/maven-repo/blob/master/releases/com/appdynamics/appd-exts-commons/1.1.2/appd-exts-commons-1.1.2.jar).
2. Run command:

   	~~~   
   	java -cp appd-exts-commons-1.1.2.jar com.appdynamics.extensions.crypto.Encryptor EncryptionKey CredentialToEncrypt

   	For example:
   	java -cp "appd-exts-commons-1.1.2.jar" com.appdynamics.extensions.crypto.Encryptor test myAwsAccessKey

   	java -cp "appd-exts-commons-1.1.2.jar" com.appdynamics.extensions.crypto.Encryptor test myAwsSecretKey
   	~~~

3. Set the decryptionKey field in config.yaml with the encryption key used, as well as the resulting encrypted awsAccessKey and awsSecretKey in their respective fields.

## Metrics
Typical metric path: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Amazon CloudSearch|\<Account Name\>|\<Region\>|ClientId|\<ClientId\>** followed by the metrics defined in the link below:

- [CloudSearch Metrics](https://docs.aws.amazon.com/cloudsearch/latest/developerguide/cloudwatch-monitoring.html#cloudsearch-metrics)

## Credentials Encryption
Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench

Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting

Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team.

## Support Tickets

If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

1. Stop the running machine agent.
2. Delete all existing logs under `<MachineAgent>/logs`.
3. Please enable debug logging by editing the file `<MachineAgent>/conf/logging/log4j.xml`. Change the level value of the following `<logger>` elements to debug.
   ```
   <logger name="com.singularity">
   <logger name="com.appdynamics">
   ```
4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory `<MachineAgent>/logs/*`.
5. Attach the zipped `<MachineAgent>/conf/*` directory here.
6. Attach the zipped `<MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith` directory here.
   
For any support related questions, you can also contact [help@appdynamics.com](mailto:help@appdynamics.com).

## Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://www.appdynamics.com/community/exchange/extension/aws-cloudsearch-monitoring-extension/).

## Version
   |Name|Version|
   |--------------------------|------------|
   |Extension Version         |2.0.2     |
   |Controller Compatibility  |4.5 or Later|
   |Agent Compatibility       |4.5 or later|
   |Last Update               |10 Jun 2021 |

