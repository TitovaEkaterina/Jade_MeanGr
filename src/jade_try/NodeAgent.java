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
 * @author titova_ekaterina
 */
public class NodeAgent extends Agent {

    private double number;
    private int countOfNodes;
    private String[] neighborsID;
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

            countOfNodes = Integer.parseInt((String) args[1]);

            if (args.length < 2 + countOfNodes) {
                doDelete();
            }
            neighborsID = new String[countOfNodes];
            for (int i = 0; i < countOfNodes; ++i) {
                neighborsID[i] = (String) args[i + 2];
                nAction.put(neighborsID[i], Boolean.FALSE);
            }
        } else {
            doDelete();
        }

        addBehaviour(new SimpleBehaviour(this) {

            public void action() {
                MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                MessageTemplate m2 = MessageTemplate.MatchOntology("Mess");
                MessageTemplate m3 = MessageTemplate.and(m1, m2);
                ACLMessage msg = blockingReceive(m3, 1200);
                if (msg != null) {

                    String[] content = msg.getContent().split(",");
                    if (!isCalculet && Double.parseDouble(content[3]) != lastoperationId) {
                        isCalculet = true;
                        lastAgetn = content[0];
                        lastoperationId = Double.parseDouble(content[3]);
                        nAction.put(content[0], Boolean.TRUE);
                        boolean isNeedToSend = false;
                        for (int i = 0; i < neighborsID.length; ++i) {
                            if (!nAction.get(neighborsID[i])) {
                                isNeedToSend = true;
                                break;
                            }
                        }
                        if (isNeedToSend) { 
                            ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                            msgNew.setOntology("Mess");
                            msgNew.setContent(getLocalName() + "," + Double.parseDouble(content[1]) + "," + Integer.parseInt(content[2]) + "," + content[3]);
                            for (int i = 0; i < neighborsID.length; i++) {
                                if (!nAction.get(neighborsID[i])) {
                                    msgNew.addReceiver(new AID(neighborsID[i] + "@localhost:1099/JADE"));
                                    nAction.put(neighborsID[i], Boolean.TRUE);

                                    ++countOfBack;
                                }
                            }
                            send(msgNew);
                        } else {
                            isCalculet = false;
                            ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                            msgNew.setOntology("BackMess");
                            msgNew.setContent(getLocalName() + "," + number + "," + 1 + "," + content[3]);
                            msgNew.addReceiver(new AID(lastAgetn + "@localhost:1099/JADE"));
                            send(msgNew);
                        }

                    } else if (lastAgetn != content[0]) {
                        ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                        msgNew.setOntology("BackMess");
                        msgNew.setContent(getLocalName() + ",0,0" + "," + content[3]);
                        msgNew.addReceiver(new AID(content[0] + "@localhost:1099/JADE"));

                        send(msgNew);
                    }
                } else {

                }

                if (isCalculet) {
                    MessageTemplate mm1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    MessageTemplate mm2 = MessageTemplate.MatchOntology("BackMess");
                    MessageTemplate mm3 = MessageTemplate.and(mm1, mm2);
                    ACLMessage msg2 = blockingReceive(mm3, 1200);
                    if (msg2 != null) {
                        String[] content = msg2.getContent().split(",");

                        summ += Double.parseDouble(content[1]);
                        count += Double.parseDouble(content[2]);
                        --countOfBack;
                        if (countOfBack == 0) {
                            ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                            msgNew.setOntology("BackMess");

                            for (int i = 0; i < neighborsID.length; ++i) {
                                nAction.put(neighborsID[i], Boolean.FALSE);
                            }

                            msgNew.setContent(getLocalName() + "," + (summ + number) + "," + (count + 1) + "," + content[3]);
                            summ = 0;
                            count = 0;
                            isCalculet = false;
                            msgNew.addReceiver(new AID(lastAgetn + "@localhost:1099/JADE"));

                            send(msgNew);
                        }
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
