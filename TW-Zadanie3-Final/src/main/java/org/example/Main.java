package org.example;

import org.jcsp.lang.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collection;

public final class Main {
    public static void main(String[] args) throws FileNotFoundException {
        int NUMBER_OF_PRODUCERS = 50;
        int NUMBER_OF_CONSUMERS = 50;
        int NUMBER_OF_BUFFERS = 8;
        int BUFFER_SIZE = 20;

        // Inicjalizacja producentów konsumentów i bufforów
        List<Producer> producers = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();
        List<Buffer> buffers = new ArrayList<>();

        for(int i = 0 ; i < NUMBER_OF_PRODUCERS ; i++)
            producers.add(new Producer(Channel.one2one(), Channel.one2one(), BUFFER_SIZE/2 ,i));

        for(int i = 0 ; i < NUMBER_OF_CONSUMERS ; i++)
            consumers.add(new Consumer(Channel.one2one(), Channel.one2one(),BUFFER_SIZE/2,i));

        for(int i = 0 ; i < NUMBER_OF_BUFFERS ; i++) {
            List<One2OneChannel<Request>> clientChannels = new ArrayList<>();

            for(int j = i; j < NUMBER_OF_CONSUMERS; j+=NUMBER_OF_BUFFERS)
                clientChannels.add( consumers.get(j).getReq());

            for(int j = i; j < NUMBER_OF_PRODUCERS; j+=NUMBER_OF_BUFFERS)
                clientChannels.add(producers.get(j).getReq());

            buffers.add(new Buffer(Channel.one2one(), Channel.one2one(), clientChannels, BUFFER_SIZE, i));
        }

        // Utworzenie cyklicznego bufora
        // buffory są połączone w topologię pierścienia
        for(int i = 0; i < NUMBER_OF_BUFFERS; i++){
            buffers.get(i).setPrev(
                buffers.get((i - 1 + NUMBER_OF_BUFFERS)%NUMBER_OF_BUFFERS).getNext()
            );

            buffers.get(i).setNext(
                    buffers.get((i + 1 + NUMBER_OF_BUFFERS)%NUMBER_OF_BUFFERS).getPrev()
            );
        }

        List<CSProcess> processList = Stream.of(buffers, producers, consumers)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        CSProcess[] convertedProcessList = new CSProcess[processList.size()];
        convertedProcessList = processList.toArray(convertedProcessList);

        Parallel par = new Parallel(convertedProcessList);
        par.run();
    }


}
