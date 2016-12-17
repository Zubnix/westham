package org.freedesktop.westfield.server;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/westfield")
public class WConnection {

    private static final String subprotocol = "westfield";
    private final WRegistry registry;

    private int nextId = 0;

    private final Map<Session, WClient> wClients = new HashMap<>();

    public WConnection() {
        this.registry = new WRegistry(nextId());
    }

    int nextId() {
        return this.nextId++;
    }

    @OnOpen
    public void onOpen(final Session session) throws IOException {
        if (!session.getNegotiatedSubprotocol()
                    .equals(subprotocol)) {
            session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR,
                                          String.format("Expected subprotocol '%s'",
                                                        subprotocol)));
        }

        final WClient client = new WClient(session);
        session.addMessageHandler(String.class,
                                  client::on);
        session.addMessageHandler(ByteBuffer.class,
                                  client::on);
        this.wClients.put(session,
                          client);

        this.registry.publishGlobals(this.registry.createResource(client));
    }


    @OnError
    public void onError(final Throwable t,
                        final Session session) {
        this.wClients.get(session)
                     .on(t);
    }

    @OnClose
    public void onClose(final Session session) {
        this.wClients.remove(session)
                     .onClose();
    }


    public Collection<WClient> getClients() {
        return this.wClients.values();
    }
}
