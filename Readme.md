# Readme

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)

## Introduction
This project provides feature to find tweets and their replies by regex on its text. It exposes two API endpoints which are as follows.

| Endpoint | Output |
|----|-----|
|/interstingTweet|returns list of tweets that matches regex, along with a 'pointer' that can be used as offset|
|/interestingConversation|returns tweets, whose text matches given regex, along with their replies and a 'pointer' that can be used as offset|

Both of this endpoints accepts POST request, and regex is to be mentioned in body of the POST request.
API also accepts other paramerters in its body, they are,
* docsToGet - Number of tweets to return.
* timeout - Time within which API should return docs (even if it cannot find docsToGet number of docs), timeout is not strict, API can take little more time than it.
* offset - It takes pointer of previous response to return next docsToGet number of tweets.

### Example
##### Request:
```ssh
 curl --location --request POST '127.0.0.1:8080/interestingConversation' \
--header 'Content-Type: application/json' \
--data-raw '{
    "regex": ".*350,000.*",
    "docsToGet": 1,
    "timeout": 1000,
    "offset":0
}'
```
##### Response:
```javascript
{
    "conversations": [
        {
            "mongoQueryId": "6225e632773cf94896629120",
            "interestingTweet": {
                "id": 1501011627977674754,
                "text": "Maybe that has something to do with the fact that everyone in the country was quarantining while 350,000 people died and COVID vaccines weren’t out yet",
                "lang": "en",
                "source": "<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>",
                "retweeted_status": null,
                "id_str": "1501011627977674754",
                "in_reply_to_status_id": null,
                "created_at": "Tue Mar 08 01:47:14 +0000 2022",
                "extended_entities": null,
                "name": "Alexandria Ocasio-Cortez",
                "screenName": "AOC",
                "profileImgUrl": "http://pbs.twimg.com/profile_images/923274881197895680/AbHcStkl_normal.jpg"
            },
            "replies": [
                {
                    "mongoQueryId": "6225e632773cf94896629120",
                    "interestingTweet": {
                        "id": 1501015816640282627,
                        "text": "Unemployment also hit 14.8% in 2020, the highest rate ever seen in the US since data collection began.\n\nDoes the Senator want to jump to claim that as Trump’s legacy too? Or would we rather examine context and data like adults? 👩🏽‍🏫",
                        "lang": "en",
                        "source": "<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>",
                        "retweeted_status": null,
                        "id_str": "1501015816640282627",
                        "in_reply_to_status_id": "1501011627977674754",
                        "created_at": "Tue Mar 08 02:03:52 +0000 2022",
                        "extended_entities": null,
                        "name": "Alexandria Ocasio-Cortez",
                        "screenName": "AOC",
                        "profileImgUrl": "http://pbs.twimg.com/profile_images/923274881197895680/AbHcStkl_normal.jpg"
                    },
                    "replies": [],
                    "tweetTime": 1646705032000,
                    "conversationId": 1501015816640282627,
                    "repliesSize": 0
                }
            ],
            "tweetTime": 1646704034000,
            "conversationId": 1501011627977674754,
            "repliesSize": 1
        }
    ],
    "pointer": 1501011627977674754
}
```

## How to Run

### Elasticsearch
Create an index of name 'conversation_sorted' with following mapping & settings:
```javascript
PUT /conversation_sorted
{
  "settings" : {
    "number_of_shards" : 4,
    "number_of_replicas" : 1,
    "index": {
      "sort.field": "conversationId", 
      "sort.order": "desc"  
    }
  }, 
  "mappings" : {
      "properties" : {
        "_class" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "conversationId" : {
          "type" : "long"
        },
        "interestingTweet" : {
          "properties" : {
            "createdAt" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "id" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "lang" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "text" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            }
          }
        },
        "mongoQueryId" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "replies" : {
          "properties" : {
            "conversationId" : {
              "type" : "long"
            },
            "interestingTweet" : {
              "properties" : {
                "createdAt" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "id" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "lang" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "parentId" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "text" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "mongoQueryId" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "tweetTime" : {
              "type" : "long"
            }
          }
        },
        "tweetTime" : {
          "type" : "long"
        }
      }
    }
}
```

Put elasticsearch's host, user and password in application.properties file, example:
```java
elastic.server=localhost:9200
elastic.password=xyz
elastic.user=elastic
```

### Redis
Put redis's host and port in application.properties file, example:
```java
redis.hostname=127.0.0.1
redis.port=6379
```

### MongoDB
Create a database named MongoQuery and collection named mongoQuery. Also, create indexes with following console commands,
```ssh
db.MongoQuery.createIndex({regex:1})
db.MongoQuery.createIndex({lastQueryTime:1})
db.MongoQuery.createIndex({groupId: 1, lastQueryTime: 1})
```

Put mongodb's connection string in application.properties file, example:
```java
mongo.connectionString=mongodb://localhost:27017/MongoQuery
```

### Kafka
Create a topic named 'sampled-stream-tweets' with 4 partition and 1 replication factor (configurable as per need)
```ssh
bin/kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --topic sampled-stream-tweets --create --partitions 4 --replication-factor 1
```

Put kafka's host in application.properties, example:
```ssh
kafka.server=localhost:9092
```

### Other properties of application

Set twitter's credential (so that App can fetch tweets from twitter).
```ssh
server.error.include-message=always
spring.output.ansi.enabled=always
twitter.bearertoken=XYZ
twitter.consumerKey=XYZ
twitter.consumerSecret=XYZ
twitter.token=XYZ
twitter.secret=XYZ
```

Other optional properties that can be altered as per need,
|Property|Use|
|---|---|
|perfTracker.enabled|(Boolean) set true if logs of performance of different DB operations are to br logged, the metrics that are logged can be configured easily in code if more metrics are required|
|kafkaconsumer.requestThresholdToStartInstantConsumer|(Integer) number of request of a particular regex to start a kafka consumer for that particular regex query, consumer will run for a max of 100 seconds and query timeout|
|kafkaconsumer.number|(Integer) number of consumer to start that will run in background, set it to a lower number than partitions of topic in kafka|

```ssh
perfTracker.enabled=false
kafkaconsumer.requestThresholdToStartInstantConsumer=1
kafkaconsumer.number=4
```


[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

[dill]: <https://github.com/joemccann/dillinger>

