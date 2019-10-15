/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jade_try;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 *
 * @author boyko_mihail
 */
public class ResourceAgent extends Agent {

    protected void setup() {
        addBehaviour(new SimpleBehaviour(this) {

            public void action() {
                ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                msgNew.setOntology("MessAboutMean");
                msgNew.setContent(getLocalName() + ",0,0,"+new Random().nextDouble());
                msgNew.addReceiver(new AID("1@localhost:1099/JADE"));
                send(msgNew);
                addBehaviour(new CyclicBehaviour(this.myAgent) {

                    @Override
                    public void action() {
                        MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                        MessageTemplate m2 = MessageTemplate.MatchOntology("BackMessAboutMean");
                        MessageTemplate m3 = MessageTemplate.and(m1, m2);
                        ACLMessage msg = blockingReceive(m3, 1200);
                        if (msg != null) {
                            System.out.println(getLocalName() + ": back message from " + msg.getSender().getLocalName() + " was received ");
                            String[] content = msg.getContent().split(",");
                             System.out.println("summ = " + Double.parseDouble(content[1]));
                              System.out.println("count = " + Double.parseDouble(content[2]) );
                            System.out.println("Mean = " + Double.parseDouble(content[1]) /Double.parseDouble(content[2]) );
                        }
                    }
                });
            }

            public boolean done() {
                return true;
            }
        });
    }
}
