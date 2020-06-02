/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package it.unitn.ds1;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unitn.ds1.Constants.*;

public class Messages {
    // Start message that sends the list of participants to everyone
    public static class StartMessage implements Serializable {
        public final List<ActorRef> group;
        public StartMessage(List<ActorRef> group) {
            this.group = Collections.unmodifiableList(new ArrayList<>(group));
        }
    }
    
    public static class VoteRequest implements Serializable {}
    
    public static class VoteResponse implements Serializable {
        public final Vote vote;
        public VoteResponse(Vote v) { vote = v; }
    }
    
    public static class DecisionRequest implements Serializable {}
    
    public static class DecisionResponse implements Serializable {
        public final Decision decision;
        public DecisionResponse(Decision d) { decision = d; }
    }
    
    public static class Timeout implements Serializable {}
    
    public static class Recovery implements Serializable {}
    
    public static class CoordinatorUpdate implements Serializable {
        public final UpdateIdentifier updateId;
        public final int data;
        
        public CoordinatorUpdate(UpdateIdentifier id, int data) {
            this.updateId = id;
            this.data = data;
        }
    }
    
    public static class UpdateACK implements Serializable {
        public final UpdateIdentifier updateId;
        
        public UpdateACK(UpdateIdentifier id) {
            this.updateId = id;
        }
    }
    
    public static class WriteOK implements Serializable {
        public final UpdateIdentifier updateId;
        
        public WriteOK(UpdateIdentifier id) {
            this.updateId = id;
        }
    }
    
    public static class ClientReadRequest implements Serializable {}
    
    public static class ClientUpdateRequest implements Serializable {
        public final int data;

        public ClientUpdateRequest(int data) {
            this.data = data;
        }
    }
    
    public static class Hearthbeat implements Serializable {}
    
    
}
