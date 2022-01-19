
# Line Messaging Bot with Google Cloud Function

Example of Line Messaging Bot hosted in Google Cloud Function(GCF). Using GCF means that the scalability part will be handled automatically by GCF.

## Prerequisites
- Create channel for messaging API in Line Developer website
- Create Google Cloud Platform and setup the permission for Google Cloud Function


## Features

- Replying TextReply Message Event automatically with echo message + " from gcp"

## Build
- Get your channel secret and channel token from Line Developer Console Website
- Run this command to deploy to GCF region Jakarta:
```shell
gcloud functions deploy <function-name> --entry-point com.linecorp.id.gcp.function.ReplyMessageEvent --runtime java11 --trigger-http --memory 512MB --allow-unauthenticated --set-env-vars CHANNELENDPOINT=https://api.line.me/,CHANNELSECRET=<your-channel-secret>,CHANNELTOKEN=<your-channel-token> --region asia-southeast2
```

- Go to the Line Developer Website, and activate the bot in your Channel Configuration. Set the webhook url to point to your GCF Function url


## Feedback
For feedback and feature request, please raise issues in the issue section of the repository. Enjoy!!.