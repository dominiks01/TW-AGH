package org.example;

import org.jcsp.lang.*;

import java.util.ArrayList;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        int NUMBER_OF_PRODUCERS = 30;
        int NUMBER_OF_CONSUMERS = 30;
        int NUMBER_OF_BUFFERS = 6;
        int BUFFER_SIZE = 6;
        int MAX_CLIENT_ACTION = BUFFER_SIZE / 2;

        List<CSProcess> processList = new ArrayList<>();
            
        for(int i = 0; i < NUMBER_OF_PRODUCERS + NUMBER_OF_CONSUMERS; i++){
            processList.add(
                (i < NUMBER_OF_PRODUCERS)?
                    new Producer(Channel.one2one(),Channel.one2one(), MAX_CLIENT_ACTION):
                    new Consumer(Channel.one2one(),Channel.one2one(), MAX_CLIENT_ACTION)
            );
        }

        List<CSProcess> buffers = new ArrayList<>();
        List<Integer> clientBuffer = new ArrayList<>();

        for(int i = 0 ; i < processList.size() ; i+=2){
            clientBuffer.add(i%NUMBER_OF_BUFFERS);
            clientBuffer.add((i+1)%NUMBER_OF_BUFFERS);
        }
        int firstBufferIndex = NUMBER_OF_PRODUCERS + NUMBER_OF_CONSUMERS;

            // TWORZENIE TOPOLOGII
            for(int i = 0 ; i < NUMBER_OF_BUFFERS ; i++) {
                List<One2OneChannel<Request>> clientIn = new ArrayList<>();
                List<One2OneChannel<Request>> clientReq = new ArrayList<>();

                for(int j = 0 ; j < clientBuffer.size() ; j++) {
                    if(clientBuffer.get(j) == i) {
                        var client = processList.get(j);

                        if (processList.get(j) instanceof Producer){
                            clientIn.add(((Producer)processList.get(j)).getIn());
                            clientReq.add(((Producer)processList.get(j)).getReq());

                        }
                        if (processList.get(j) instanceof Consumer){
                            clientIn.add(((Consumer)processList.get(j)).getIn());
                            clientReq.add(((Consumer)processList.get(j)).getReq());
                        }
                    }
                }
                One2OneChannel forwardChannel = null;
                One2OneChannel backwardChannel = null;

                if(i == 0) {
                    forwardChannel = Channel.one2one();
                    backwardChannel = Channel.one2one();
                }

                if(i > 0 && i < NUMBER_OF_BUFFERS - 1) {
                    forwardChannel = Channel.one2one();
                    backwardChannel = ((Buffer) processList.get(firstBufferIndex + i - 1)).getNext();
                }

                if(i == NUMBER_OF_BUFFERS - 1) {
                    forwardChannel = ((Buffer) processList.get(firstBufferIndex)).getPrev();
                    backwardChannel = ((Buffer) processList.get(firstBufferIndex + i - 1)).getNext();
                }
                processList.add(new Buffer(forwardChannel, backwardChannel, clientIn, clientReq, BUFFER_SIZE, i));
            }

            CSProcess[] convertedProcessList = new CSProcess[processList.size()];
            convertedProcessList = processList.toArray(convertedProcessList);

            Parallel par = new Parallel(convertedProcessList);
            par.run();
        }


    }
