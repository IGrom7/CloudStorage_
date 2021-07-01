package ru.grom.cloudstorage.lesson3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

public class StringInputHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);
//        mainActions(ctx, message);
        System.out.println("message: " + message.replace("\n", ""));
        ctx.write(message);
    }



}
