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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author boyko_mihail
 */
public class OrderAgent extends Agent {

    private double number;
    private int countOfNeighbors;
    private String[] neighborsIdentifire;
    private boolean isCalculet = false;
    private String lastAgetn = "";
    private int countOfBack = 0;
    private double summ = 0;
    private double count = 0;
    private double lastoperationId = 0;
    private Map<String, Boolean> nAction = new HashMap<>();

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            number = Integer.parseInt((String) args[0]);
            System.out.println("Number is " + number);

            countOfNeighbors = Integer.parseInt((String) args[1]);
            System.out.println("countOfNeighbors is " + countOfNeighbors);

            if (args.length < 2 + countOfNeighbors) {
                System.out.println("No neighborsdentifire");
                doDelete();
            }
            neighborsIdentifire = new String[countOfNeighbors];
            for (int i = 0; i < countOfNeighbors; ++i) {
                neighborsIdentifire[i] = (String) args[i + 2];
                nAction.put(neighborsIdentifire[i], Boolean.FALSE);
            }
        } else {
            System.out.println("No number");
            doDelete();
        }

        addBehaviour(new SimpleBehaviour(this) {

            public void action() {
                MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                MessageTemplate m2 = MessageTemplate.MatchOntology("MessAboutMean");
                MessageTemplate m3 = MessageTemplate.and(m1, m2);
                ACLMessage msg = blockingReceive(m3, 1200);
                if (msg != null) {
                    
                    String[] content = msg.getContent().split(",");
                    System.out.println("MessAboutMean give: " + msg.getSender().getLocalName() + "->" + getLocalName() + " summ = " + content[1]);
                    
                    if (!isCalculet && Double.parseDouble(content[3]) != lastoperationId) {
                        isCalculet = true;
                        lastAgetn = content[0];
                        lastoperationId = Double.parseDouble(content[3]);
                        nAction.put(content[0], Boolean.TRUE);
                        boolean isNeedToSend = false;
                        for (int i = 0; i < neighborsIdentifire.length; ++i) {
                            if (!nAction.get(neighborsIdentifire[i])) {
                                isNeedToSend = true;
                                break;
                            }
                        }
                        if (isNeedToSend) {
                            ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                            msgNew.setOntology("MessAboutMean");
                            msgNew.setContent(getLocalName() + "," + Double.parseDouble(content[1]) + "," + Integer.parseInt(content[2]) + "," + content[3]);
                            for (int i = 0; i < neighborsIdentifire.length; i++) {
                                if (!nAction.get(neighborsIdentifire[i])) {
                                    msgNew.addReceiver(new AID(neighborsIdentifire[i] + "@localhost:1099/JADE"));
                                    nAction.put(neighborsIdentifire[i], Boolean.TRUE);
                                     
                                    System.out.println("MessAboutMean send: " + msg.getSender().getLocalName() + "->" + getLocalName() + " summ = " + content[1]);
                                    ++countOfBack;
                                }
                            }
                            send(msgNew);
                        } else {
                             isCalculet = false;
                            ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                            msgNew.setOntology("BackMessAboutMean");
                            msgNew.setContent(getLocalName() + "," + number + "," + 1 + "," + content[3] );
                            msgNew.addReceiver(new AID(lastAgetn + "@localhost:1099/JADE"));
                            System.out.println("BackMessAboutMean send: " + getLocalName() + "->" + lastAgetn + " summ = " + number);
                            send(msgNew);
                        }

                    } else if (lastAgetn != content[0]) {
                        ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                        msgNew.setOntology("BackMessAboutMean");
                        msgNew.setContent(getLocalName() + ",0,0" + "," + content[3]);
                         System.out.println("BackMessAboutMean send: " + getLocalName() + "->" + content[0] + " summ = " + 0);
                        msgNew.addReceiver(new AID(content[0] + "@localhost:1099/JADE"));

                        send(msgNew);
                    } 
                } else {
                    //System.out.println("Emphty messege from " + getLocalName());
                }
                System.out.println( getLocalName() + " isCalculet = " + isCalculet );
                if (isCalculet) {
                   // System.out.println( getLocalName() + " isCalculet = " + isCalculet );
                    MessageTemplate mm1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    MessageTemplate mm2 = MessageTemplate.MatchOntology("BackMessAboutMean");
                    MessageTemplate mm3 = MessageTemplate.and(mm1, mm2);
                    ACLMessage msg2 = blockingReceive(mm3, 1200);
                    if (msg2 != null) {
                        System.out.println("BackMessAboutMean give: " + msg2.getSender().getLocalName() + "->" + getLocalName() + " summ = " + 0);
   
                        String[] content = msg2.getContent().split(",");

                        summ += Double.parseDouble(content[1]);
                         System.out.println(getLocalName() + " summ = " + summ);
                        count += Double.parseDouble(content[2]);
                        System.out.println(getLocalName() + " count = " + count);
                        --countOfBack;
                         System.out.println(getLocalName() + " countOfBack = " + countOfBack);

                        if (countOfBack == 0) {
                            ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                            msgNew.setOntology("BackMessAboutMean");

                            for (int i = 0; i < neighborsIdentifire.length; ++i) {
                                nAction.put(neighborsIdentifire[i], Boolean.FALSE);
                            }

                            //if (isCalculet) {
                                msgNew.setContent(getLocalName() + "," + (summ + number) + "," + (count + 1) + "," + content[3]);
                            //}
                            summ = 0;
                            count = 0;
                            isCalculet = false;
                            msgNew.addReceiver(new AID(lastAgetn + "@localhost:1099/JADE"));
                            System.out.println(getLocalName() + " SEND BACK TO  " + lastAgetn);
                            System.out.println("BackMessAboutMean give: " + msg2.getSender().getLocalName() + "->" + lastAgetn+ " summ = " + (summ + number));
   

                            send(msgNew);
                        }
                    } else {
                        //System.out.println("Emphty back messege from " + getLocalName());
                    }
                }

            }

            @Override
            public boolean done() {
                return false;
            }
        });
    }
}
