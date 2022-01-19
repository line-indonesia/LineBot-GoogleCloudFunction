package com.linecorp.id.gcp.function;

import java.io.BufferedWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.parser.WebhookParser;

public class ReplyMessageEvent implements HttpFunction {

    private LineMessagingClient client1;
    private LineSignatureValidator sigValidator;
    private WebhookParser parser1;    
    private String channelToken;
    private String apiEndPoint;

    private Logger log1=LoggerFactory.getLogger(ReplyMessageEvent.class);

    public ReplyMessageEvent() throws URISyntaxException {
        channelToken=System.getenv("CHANNELTOKEN");
        apiEndPoint=System.getenv("CHANNELENDPOINT");
        String channelScret=System.getenv("CHANNELSECRET");
        
        this.apiEndPoint=Optional.ofNullable(apiEndPoint).orElse("https://api.line.me/");
        
        log1.debug("Channel Token:"+channelToken);
        log1.debug("Line Endpoint:"+apiEndPoint);
        log1.debug("Channel Secret:"+channelScret);

        this.client1=LineMessagingClient.builder(channelToken).apiEndPoint(new URI(this.apiEndPoint)).build();                        

        this.sigValidator=new LineSignatureValidator(channelScret.getBytes());
        this.parser1=new WebhookParser(sigValidator);
        

    }

    public void service(HttpRequest req, HttpResponse resp) throws Exception {
        try {
            final byte[] json = ByteStreams.toByteArray(req.getInputStream());
            if(!req.getMethod().equalsIgnoreCase("POST"))   {

                log1.debug("Request content:"+new String(json, StandardCharsets.UTF_8));
                resp.setStatusCode(405);
                BufferedWriter bw=resp.getWriter();
                bw.write("Method not supported");
            }else   {
                resp.setStatusCode(200);
                String signature=req.getHeaders().get(WebhookParser.SIGNATURE_HEADER_NAME).get(0);            
                log1.debug("Request content:"+new String(json, StandardCharsets.UTF_8));

                //Parse request
                CallbackRequest cbr=parser1.handle(signature, json);

                if(cbr.getEvents().size()>0)    {
                    cbr.getEvents().forEach(event->{
                        ReplyMessage rm=getResponse(event);
                        if(rm!=null)    {
                            BotApiResponse bot1;
                            try {
                                bot1 = client1.replyMessage(rm).get();
                                log1.debug("BotResponse:"+bot1.getMessage()+":"+bot1.getRequestId());                                
                                log1.info("Successfully sending reply message for replyToken:{}",rm.getReplyToken());
                            } catch (Exception e) {
                                log1.error("Failed to send response to replyId:{}",rm.getReplyToken(),e);
                            }


                        }
                    });


                }
            }
        }
        catch(Exception e)  {
            log1.error("Error while processing Http Request",e);
            resp.setStatusCode(500);
        }

    }


    private ReplyMessage getResponse(Event event1)   {
        //Only reply for Event Message
        if(event1 instanceof MessageEvent)  {
            MessageEvent me1=(MessageEvent) event1;         
            log1.debug("reply-token:"+me1.getReplyToken());
            if(me1.getMessage() instanceof TextMessageContent) {
                TextMessageContent tmc=(TextMessageContent) me1.getMessage();
                String reply=tmc.getText()+" from gcp";
                TextMessage tm=new TextMessage(reply);
                ReplyMessage rm=new ReplyMessage(me1.getReplyToken(),tm);
                return rm;
            }
            else   {
                TextMessage tm=new TextMessage("Jenis pesan tidak di-support oleh bot ini");
                ReplyMessage rm=new ReplyMessage(me1.getReplyToken(),tm);
                return rm;
            }
        }
        else    {
            return null;
        }
    }


}
