package me.lab7.client;

import me.lab7.common.data.LabWork;
import me.lab7.common.Encoder;
import me.lab7.common.Request;
import me.lab7.common.RequestWithCommands;
import me.lab7.common.RequestWithLabWork;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Sender {
    private SocketChannel socket;
    private ByteBuffer sizeIntBuffer = ByteBuffer.allocate(Integer.BYTES);
    private ByteBuffer payloadBuffer = null;
    public Sender() {
    }

    public void sendMessage(String[] command) throws IOException {
        ByteBuffer buffer = Encoder.encode(new Request(command));
        buffer.flip();
        while (buffer.hasRemaining()) {
            socket.write(buffer);
        }
    }

    public void sendMessageWithLabWork(LabWork labWork) throws IOException {
        ByteBuffer buffer = Encoder.encode(new RequestWithLabWork("add", labWork));
        buffer.flip();
        while (buffer.hasRemaining()) {
            socket.write(buffer);
        }
    }
    public void sendMessageWithCommands(ArrayList<String> commands) throws IOException {
        ByteBuffer buffer = Encoder.encode(new RequestWithCommands(commands));
        buffer.flip();
        while (buffer.hasRemaining()) {
            socket.write(buffer);
        }
    }
    public boolean checkForMessage() throws IOException {
        // No need to check anything if payload is already read.
        if (payloadBuffer != null && !payloadBuffer.hasRemaining()) {
            return true;
        }

        // Try to read the entire header containing number of bytes in payload
        socket.read(sizeIntBuffer);
        if (sizeIntBuffer.hasRemaining()) {
            return false;
        }

        // Header is received, generate the payload buffer
        if (payloadBuffer == null) {
            payloadBuffer = ByteBuffer.allocate(sizeIntBuffer.getInt(0));
        }

        // Try to read to payload buffer
        socket.read(payloadBuffer);
        return !payloadBuffer.hasRemaining();
    }

    public Object getPayload() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(payloadBuffer.array());
        ObjectInputStream ois = new ObjectInputStream(bais);

        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    public void clearInBuffer() {
        ((Buffer) sizeIntBuffer).clear();
        payloadBuffer = null;
    }
    public void setSocket(SocketChannel socket){
        this.socket = socket;
    }

}
