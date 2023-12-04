package org.example;

import org.jcsp.lang.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collection;

public final class Main {
    public static void main(String[] args) {
        int NUMBER_OF_PRODUCERS = 30;
        int NUMBER_OF_CONSUMERS = 30;
        int NUMBER_OF_BUFFERS = 6;
        int BUFFER_SIZE = 6;
        int MAX_CLIENT_ACTION = BUFFER_SIZE / 2;

        List<CSProcess> processList = new ArrayList<>();

        for(int i = 0 ; i < NUMBER_OF_PRODUCERS ; i++) {
            processList.add(new Producer(Channel.one2one(), Channel.one2one(),MAX_CLIENT_ACTION,i));
        }

        for(int i = 0 ; i < NUMBER_OF_CONSUMERS ; i++) {
            processList.add(new Consumer(Channel.one2one(), Channel.one2one(),MAX_CLIENT_ACTION,i));
        }

        List<Integer> clientBuffer = new ArrayList<>();
        int bufferIndex = 0;
        for(int i = 0 ; i < processList.size() ; i++){  // dla każdego klienta
            System.out.println(bufferIndex);
            clientBuffer.add(bufferIndex);
            bufferIndex = (bufferIndex + 1) % NUMBER_OF_BUFFERS;
        }

        int firstBufferIndex = NUMBER_OF_PRODUCERS + NUMBER_OF_CONSUMERS;

        // TWORZENIE TOPOLOGII
        for(int i = 0 ; i < NUMBER_OF_BUFFERS ; i++) {
            List<One2OneChannel<Request>> clientChannels = new ArrayList<>();

            for(int j = 0 ; j < clientBuffer.size() ; j++) {
                if(clientBuffer.get(j) == i) {
                    var client = processList.get(j);
                    if(client instanceof Producer) {
                        clientChannels.add(((Producer) processList.get(j)).getReq());
                    } else if(client instanceof Consumer) {
                        clientChannels.add(((Consumer) processList.get(j)).getReq());
                    }
                }
            }
            One2OneChannel forwardChannel = null;
            One2OneChannel backwardChannel = null;

            if(i == 0) {
                forwardChannel = Channel.one2one();
                backwardChannel = Channel.one2one();
            } else if(i > 0 && i < NUMBER_OF_BUFFERS - 1) {
                forwardChannel = Channel.one2one();
                backwardChannel = ((Buffer) processList.get(firstBufferIndex + i - 1)).getNext_buffer();
            } else if(i == NUMBER_OF_BUFFERS - 1) {
                forwardChannel = ((Buffer) processList.get(firstBufferIndex)).getPrev_buffer();
                backwardChannel = ((Buffer) processList.get(firstBufferIndex + i - 1)).getNext_buffer();
            }
            processList.add(new Buffer(forwardChannel, backwardChannel, clientChannels, BUFFER_SIZE, i));
        }

        CSProcess[] convertedProcessList = new CSProcess[processList.size()];
        convertedProcessList = processList.toArray(convertedProcessList);

        Parallel par = new Parallel(convertedProcessList);
        par.run();
    }


}
