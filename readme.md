A project extension for the Muse Test Framework and MuseIDE. Adds tools for collecting and storing measurements of events in a Muse test. Also allows configuring performance goals and assessing the performance of the system against those goals. 

# Capabilities

This extension adds these capabilities (Context Initializers) to a project:

1. Step Duration Collector
2. Step Duration Goal Assessor

To turn on one of these features for a project:

1. Add a Context Initializer group if the project does not already have one.
2. Within the initializer group, add an initializer with desired type.

# Step Duration Collector

Collects the durations of steps and stores them with the test results.

### Parameters

none 

### Example configuration file

    {
    "type" : "context-initializers",
    "applyToTestCondition" : 
		{
    	"value" : true,
    	"type" : "boolean"
    	},
  	"initializers" : [ 
		{
    	"parameters" : { },
    	"typeId" : "wpi.measurements.step-durations",
    	"applyCondition" : 
			{
      		"value" : true,
	      	"type" : "boolean"
    		}
      	} ],
      "tags" : [ ]
    }

### Example in MuseIDE

![](images/readme-stepdurationcollector-museide.png)

# Step Duration Goal Assessor

Compares the duration of steps to a performance goal and adds a failure to the test if the goal is exceeded.

### Parameters

**goal**: (required) The default performance goal, in milliseconds, for assessing the performance of each step.

**step-has-tag**: (optional) Assess goals only if the step is tagged with the value of this parameter.

**step-goal-name**: (optional) Allows overriding the default goal with a custom goal for each step. The value of this parameter should be used as the name of a metadata attribute on the step. The value of that attribute will be used as the goal for the step (it should be provided as an integer in milliseconds).

### Example configuration file

    {
      "type" : "context-initializers",
      "applyToTestCondition" : {
        "value" : true,
        "type" : "boolean"
      },
      "initializers" : [ {
        "parameters" : {
          "goal" : {
            "value" : 200,
            "type" : "integer"
          },
          "step-has-tag" : {
            "value" : "assess-goal",
            "type" : "string"
          },
          "step-goal-name" : {
            "value" : "duration-goal",
            "type" : "string"
          }
        },
        "typeId" : "wpi.measurements.step-duration-goal-assessor",
        "applyCondition" : {
          "value" : true,
          "type" : "boolean"
        }
      } ],
      "tags" : [ ]
    }

### Example in MuseIDE

In this example:

* the default step duration goal is 200ms
* only steps tagged with *assess-goal* will be evaluated
* if the step has a *duration-goal* attribute, then the value of that attribute will be used for the goal, rather than the 200ms default

![](images/readme-stepdurationgoalassessor-museide.png)

Here is an example of the step configured with:

* the *assess-goal* tag to indicate this step should be assessed for performance
* a *duration-goal* attribute to use a goal of 100ms for this step instead of the default

![](images/readme-stepdurationgoalassessor-museide-stepconfig.png)

