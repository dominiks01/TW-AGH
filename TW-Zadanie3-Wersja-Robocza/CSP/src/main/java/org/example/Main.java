package org.example;

import org.jcsp.lang.*;

import java.util.ArrayList;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        int NUMBER_OF_PRODUCERS = 10;
        int NUMBER_OF_CONSUMERS = 10;
        int NUMBER_OF_BUFFERS = 6;
        int BUFFER_SIZE = 6;
        int MAX_CLIENT_ACTION = BUFFER_SIZE / 2;

        List<CSProcess> process_list = new ArrayList<>();

        for(int i = 0; i < NUMBER_OF_PRODUCERS + NUMBER_OF_CONSUMERS; i++){
            DualChannel dual_channel = new DualChannel(Channel.one2one(), Channel.one2one());
            
            process_list.add(
                (i < NUMBER_OF_PRODUCERS)?
                    new Producer(dual_channel, MAX_CLIENT_ACTION, i):
                    new Consumer(dual_channel, MAX_CLIENT_ACTION, i)
            );
        }

        List<Integer> client_buffer = new ArrayList<>();
        for(int i = 0 ; i < process_list.size() ; i++){ 
            System.out.println(i % NUMBER_OF_BUFFERS);
            client_buffer.add(i % NUMBER_OF_BUFFERS);
        }

        int firstBufferIndex = NUMBER_OF_PRODUCERS + NUMBER_OF_CONSUMERS;

        for(int i = 0 ; i < NUMBER_OF_BUFFERS ; i++) {
            List<DualChannel> client_channels = new ArrayList<>();
            for(int j = 0 ; j < client_buffer.size() ; j++) {
                if(client_buffer.get(j) == i) {

                    client_channels.add(
                        (process_list.get(j) instanceof Consumer)?
                            ((Consumer)process_list.get(j)).getBuffer():
                            ((Producer)process_list.get(j)).getBuffer()
                    );
                }
            }
            One2OneChannel<Batch> next_buffor = null;
            One2OneChannel<Batch> prev_buffor = null;

            if(i == 0) {
                next_buffor = Channel.one2one();
                prev_buffor = Channel.one2one();
            }
            
            if(i > 0 && i < NUMBER_OF_BUFFERS - 1) {
                next_buffor = Channel.one2one();
                prev_buffor = ((Buffer) process_list.get(firstBufferIndex + i - 1)).getForwardBuffer();
            }
            
            if(i == NUMBER_OF_BUFFERS - 1) {
                next_buffor = ((Buffer) process_list.get(firstBufferIndex)).getBackwardBuffer();
                prev_buffor = ((Buffer) process_list.get(firstBufferIndex + i - 1)).getForwardBuffer();
            }
            
            process_list.add(
                new Buffer(next_buffor, prev_buffor, client_channels, BUFFER_SIZE, i)
            );
        }

        Parallel par = new Parallel(process_list.stream().toArray(CSProcess[]::new));
        par.run();
    }
}
