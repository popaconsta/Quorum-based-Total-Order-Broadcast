/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package it.unitn.ds1;

public class Constants {
    public final static int N_NODES = 5;
    public final static int INITIAL_COORDINATOR_ID = 0;
    public final static int VOTE_TIMEOUT = 1000;      // timeout for the votes, ms
    public final static int DECISION_TIMEOUT = 2000;  // timeout for the decision, ms
    public final static int WRITE_OK_TIMEOUT = 1500;
    public final static int UPDATE_REQUEST_TIMEOUT = 1500;
    
    
    public enum Vote {NO, YES}
    public enum Decision {ABORT, COMMIT}
    
    // the votes that the participants will send (for testing)
    public final static Vote[] predefinedVotes =
            new Vote[] {Vote.YES, Vote.YES, Vote.YES}; // as many as N_PARTICIPANTS
}
