/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unitn.ds1;

import java.io.Serializable;

/**
 *
 * @author invidia
 */
public class UpdateIdentifier implements Serializable {
    public final long e;
    public final long i;
    
    public UpdateIdentifier(long e, long i) { 
        this.e = e;
        this.i = i;
    }
}
