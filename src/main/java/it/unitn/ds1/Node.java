/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package it.unitn.ds1;

import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.unitn.ds1.Messages.*;
import it.unitn.ds1.Constants.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Node extends AbstractActor {
    private int id;
    private int e;
    private int i;
    private List<ActorRef> replicas;
    private ActorRef coordinator;
    
    private HashMap<UpdateIdentifier, Integer> pendingUpdates = null;
    private HashMap<UpdateIdentifier, Integer> commitHistory = null;
    private HashMap<UpdateIdentifier, Set<ActorRef>> updateACKs = null;
    
    
    
    public Node(int id) {
        super();
        this.id = id;
        this.e = 0;
        this.i = 0;
        pendingUpdates = new HashMap<>();
        commitHistory = new HashMap<>();
        updateACKs = new HashMap<>();
    }
    
    static public Props props(int id) {
        return Props.create(Node.class, () -> new Node(id));
    }
    
    
    void setGroup(StartMessage sm) {
        replicas = new ArrayList<>();
        for (ActorRef b: sm.group) {
            if (!b.equals(getSelf())) {
                
                // copying all participant refs except for self
                this.replicas.add(b);
            }
        }
        print("Starting with " + sm.group.size() + " peer(s)");
    }
    
    void multicast(Serializable m) {
        for (ActorRef p: replicas)
            p.tell(m, getSelf());
    }
    
    public void onStartMessage(StartMessage msg) {                   /* Start */
        setGroup(msg);
        coordinator = msg.group.get(Constants.INITIAL_COORDINATOR_ID);
        if(id == Constants.INITIAL_COORDINATOR_ID)
            getContext().become(coordinator());
        else
            getContext().become(replica());
        
        /*
        print("Sending vote request");
        multicast(new VoteRequest());
        //multicastAndCrash(new VoteRequest(), 3000);
        setTimeout(Constants.VOTE_TIMEOUT);
        //crash(5000);
        */
    }
    
    public void onClientUpdateRequest(ClientUpdateRequest updateReq) {
        
        if(getSelf() != coordinator) {
            print("Forwarding request to update with val=" + updateReq.data + " to coordinator");
            coordinator.tell(updateReq, getSelf());
        } else {
            print("Request to update with val=" + updateReq.data + " received by coordinator");
            UpdateIdentifier updateId = new UpdateIdentifier(e, i++);
            pendingUpdates.put(updateId, updateReq.data);
            Set<ActorRef> acks = new HashSet<>();
            acks.add(getSelf()); //add coordinator to ACK list
            updateACKs.put(updateId, acks);
            multicast(new CoordinatorUpdate(updateId, updateReq.data));
        }
    }
    
    public void onCoordinatorUpdate(CoordinatorUpdate update) {
        print("Update(e=" + update.updateId.e + ", i=" + update.updateId.i + ") with val=" + update.data + " received from coordinator");
        
        coordinator.tell(new UpdateACK(update.updateId), getSelf());
    }
    
    public void onUpdateACK(UpdateACK updateACK) {
        
        UpdateIdentifier updateId = updateACK.updateId;
        //print("Acknowledge for update (e=" + updateId.e + ", i=" + updateId.i + ") received from " + getSender().path().name());
        
        //Check if update isn't commited already
        if(pendingUpdates.get(updateId) != null){
            print("Acknowledge for update (e=" + updateId.e + ", i=" + updateId.i + ") received from " + getSender().path().name());
       
            Set<ActorRef> acks = updateACKs.getOrDefault(updateId, new HashSet<>());
            acks.add(getSender()); //add ack to the list
            updateACKs.put(updateId, acks);
            int quorumSize = Math.round((replicas.size() + 1) / 2) + 1;
            if(acks.size() >= quorumSize) {
                int data = pendingUpdates.get(updateId);
                pendingUpdates.remove(updateId);
                commitHistory.put(updateId, data);
                multicast(new WriteOK(updateId));
            }
        }
    }
    
    public void onWriteOK(WriteOK writeOK) {
        print("Updating data according to (e=" + writeOK.updateId.e + ", i=" + writeOK.updateId.i + ") as told by coordinator");
    }
    
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartMessage.class, this::onStartMessage)
                .match(ClientUpdateRequest.class, this::onClientUpdateRequest)
                //.matchAny(msg -> {})
                .build();
    }
    
    public Receive replica() {
        return receiveBuilder()
                .match(StartMessage.class, this::onStartMessage)
                .match(ClientUpdateRequest.class, this::onClientUpdateRequest)
                .match(CoordinatorUpdate.class, this::onCoordinatorUpdate)
                .match(WriteOK.class, this::onWriteOK)
                //.match(Timeout.class, this::onTimeout)
                .build();
    }
    
    public Receive coordinator() {
        return receiveBuilder()
                .match(StartMessage.class, this::onStartMessage)
                .match(ClientUpdateRequest.class, this::onClientUpdateRequest)
                .match(UpdateACK.class, this::onUpdateACK)
                //.match(Timeout.class, this::onTimeout)
                .build();
    }
    
    // a simple logging function
    void print(String s) {
        String role = coordinator == getSelf() ? "c" : "r";
        System.out.format("Node%2d(%s): %s\n", id, role, s);
    }
    
    /*
    
    // emulate a crash and a recovery in a given time
    void crash(int recoverIn) {
    getContext().become(crashed());
    print("CRASH!!!");
    
    // setting a timer to "recover"
    getContext().system().scheduler().scheduleOnce(
    Duration.create(recoverIn, TimeUnit.MILLISECONDS),
    getSelf(),
    new Recovery(), // message sent to myself
    getContext().system().dispatcher(), getSelf()
    );
    }
    
    // emulate a delay of d milliseconds
    void delay(int d) {
    try {Thread.sleep(d);} catch (Exception ignored) {}
    }
    
    
    
    // a multicast implementation that crashes after sending the first message
    void multicastAndCrash(Serializable m, int recoverIn) {
    for (ActorRef p: replicas) {
    p.tell(m, getSelf());
    crash(recoverIn); return;
    }
    }
    
    // schedule a Timeout message in specified time
    void setTimeout(int time) {
    getContext().system().scheduler().scheduleOnce(
    Duration.create(time, TimeUnit.MILLISECONDS),
    getSelf(),
    new Timeout(), // the message to send
    getContext().system().dispatcher(), getSelf()
    );
    }
    
    public void onVoteResponse(VoteResponse msg) {
    if (hasDecided()) {
    
    // we have already decided and sent the decision to the group,
    // so do not care about other votes
    return;
    }
    Vote v = (msg).vote;
    if (v == Vote.YES) {
    yesVoters.add(getSender());
    if (allVotedYes()) {
    fixDecision(Decision.COMMIT);
    if (id==-1) {crash(3000); return;}
    multicast(new DecisionResponse(decision));
    //multicastAndCrash(new DecisionResponse(decision), 3000);
    }
    }
    else { // a NO vote
    
    // on a single NO we decide ABORT
    fixDecision(Decision.ABORT);
    multicast(new DecisionResponse(decision));
    }
    }
    
    public void onTimeout(Timeout msg) {
    if (!hasDecided()) {
    print("Timeout");
    
    // not decided in time means ABORT
    fixDecision(Decision.ABORT);
    multicast(new DecisionResponse(Decision.ABORT));
    }
    }
    
    public void onVoteRequest(VoteRequest msg) {
    this.coordinator = getSender();
    //if (id==2) {crash(5000); return;}    // simulate a crash
    //if (id==2) delay(4000);              // simulate a delay
    if (Constants.predefinedVotes[this.id] == Vote.NO) {
    fixDecision(Decision.ABORT);
    }
    print("sending vote " + Constants.predefinedVotes[this.id]);
    this.coordinator.tell(new VoteResponse(Constants.predefinedVotes[this.id]), getSelf());
    setTimeout(Constants.DECISION_TIMEOUT);
    }
    
    public void onDecisionRequest(DecisionRequest msg) {
    if (hasDecided())
    getSender().tell(new DecisionResponse(decision), getSelf());
    
    // just ignoring if we don't know the decision
    }
    
    public Receive crashed() {
    return receiveBuilder()
    .matchAny(msg -> {})
    .build();
    }
    */
}