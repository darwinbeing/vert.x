package vertx.tests.busmods.mailer;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.JsonMessage;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.newtests.TestClientBase;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestClient extends TestClientBase {

  private EventBus eb = EventBus.instance;

  @Override
  public void start() {
    super.start();
    tu.appReady();
  }

  @Override
  public void stop() {
    super.stop();
  }

  public void testSendMultiple() throws Exception {
    final int numMails = 10;
    Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
      int count;
      public void handle(Message<JsonObject> message) {
        tu.checkContext();
        tu.azzert(message.body.getString("status").equals("ok"));
        if (++count == numMails) {
          tu.testComplete();
        }
      }
    };
    for (int i = 0; i < numMails; i++) {
      JsonObject jsonObject = createBaseMessage();
      eb.send("test.mailer", jsonObject, replyHandler);
    }
  }

  public void testSendWithSingleRecipient() throws Exception {
    String rec = System.getProperty("user.name") + "@localhost";
    JsonObject jsonObject = new JsonObject().putString("to", rec);
    send(jsonObject, null);
  }

  public void testSendWithRecipientList() throws Exception {
    String rec = System.getProperty("user.name") + "@localhost";
    JsonArray recipients = new JsonArray(new String[] { rec, rec, rec });
    JsonObject jsonObject = new JsonObject().putArray("to", recipients);
    send(jsonObject, null);
  }

  public void testSendWithSingleCC() throws Exception {
    String rec = System.getProperty("user.name") + "@localhost";
    JsonObject jsonObject = new JsonObject().putString("to", rec).putString("cc", rec);
    send(jsonObject, null);
  }

  public void testSendWithCCList() throws Exception {
    String rec = System.getProperty("user.name") + "@localhost";
    JsonArray recipients = new JsonArray(new String[] { rec, rec, rec });
    JsonObject jsonObject = new JsonObject().putArray("cc", recipients);
    send(jsonObject, null);
  }

  public void testSendWithSingleBCC() throws Exception {
    String rec = System.getProperty("user.name") + "@localhost";
    JsonObject jsonObject = new JsonObject().putString("to", rec).putString("bcc", rec);
    send(jsonObject, null);
  }

  public void testSendWithBCCList() throws Exception {
    String rec = System.getProperty("user.name") + "@localhost";
    JsonArray recipients = new JsonArray(new String[] { rec, rec, rec });
    JsonObject jsonObject = new JsonObject().putArray("bcc", recipients);
    send(jsonObject, null);
  }

  public void testInvalidSingleFrom() throws Exception {
    JsonObject jsonObject = new JsonObject().putString("from", "wqdqwd qwdqwd qwdqwd ");
    send(jsonObject, "Invalid from");
  }

  public void testInvalidSingleRecipient() throws Exception {
    JsonObject jsonObject = new JsonObject().putString("to", "wqdqwd qwdqwd qwdqwd ");
    send(jsonObject, "Invalid to");
  }

  public void testInvalidRecipientList() throws Exception {
    JsonArray recipients = new JsonArray(new String[] { "tim@localhost", "qwdqwd qwdqw d", "qwdkiwqdqwd d" });
    JsonObject jsonObject = new JsonObject().putArray("to", recipients);
    send(jsonObject, "Invalid to");
  }

  private void send(JsonObject overrides, final String error) throws Exception {
    Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
      int count;
      public void handle(Message<JsonObject> message) {
        tu.checkContext();
        if (error == null) {
          tu.azzert(message.body.getString("status").equals("ok"));
        } else {
          tu.azzert(message.body.getString("status").equals("error"));
          tu.azzert(message.body.getString("message").startsWith(error));
        }
        tu.testComplete();
      }
    };
    JsonObject jsonObject = createBaseMessage();
    jsonObject.mergeIn(overrides);
    eb.send("test.mailer", jsonObject, replyHandler);
  }

  private JsonObject createBaseMessage() {
    String user = System.getProperty("user.name");
    JsonObject jsonObject = new JsonObject().putString("from", user + "@localhost").putString("to", user + "@localhost")
        .putString("subject", "This is a test").putString("payload", "This is the payload\nof the mail");
    return jsonObject;
  }

}
