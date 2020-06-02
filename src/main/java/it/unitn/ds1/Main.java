package it.unitn.ds1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unitn.ds1.Constants.*;
import it.unitn.ds1.Messages.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Random rand = new Random();
        
        // Create the actor system
        final ActorSystem system = ActorSystem.create("qbtob");
        
        // Create the coordinator
        //ActorRef coordinator = system.actorOf(Coordinator.props(), "coordinator");
        
        // Create nodes
        List<ActorRef> group = new ArrayList<>();
        for (int i=0; i<Constants.N_NODES; i++) {
            group.add(system.actorOf(Node.props(i), "node" + i));
        }
        
        // Send start messages to the participants to inform them of the group
        StartMessage start = new StartMessage(group);
        for (ActorRef node: group) {
            node.tell(start, null);
        }
        
        Cancellable cancellable = system.scheduler().scheduleOnce(
                Duration.create(500, TimeUnit.MILLISECONDS),
                group.get(0), //coordinator
                new ClientUpdateRequest(1),
                system.dispatcher(),
                ActorRef.noSender()
        );
        
        cancellable = system.scheduler().scheduleOnce(
                Duration.create(2000, TimeUnit.MILLISECONDS),
                group.get(1), //coordinator
                new ClientUpdateRequest(2),
                system.dispatcher(),
                ActorRef.noSender()
        );
        
        cancellable = system.scheduler().scheduleOnce(
                Duration.create(4000, TimeUnit.MILLISECONDS),
                group.get(2), //coordinator
                new ClientUpdateRequest(3),
                system.dispatcher(),
                ActorRef.noSender()
        );
        
        
        
        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        }
        catch (IOException ignored) {}
        system.terminate();
    }
}
