//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Hlaaftana {
    public static JDA client;
    public static Map<String, Message> messages = new HashMap();

    public Hlaaftana() {
    }

    public static void main(String[] args) throws LoginException, InterruptedException, RateLimitedException {
        if (args.length < 1) {
            System.out.println("You forgot to add your token as a command argument.");
            System.exit(1);
        }

        client = (new JDABuilder(AccountType.CLIENT)).setToken(args[0]).addEventListener(new Object[]{new ListenerAdapter() {
            public void onMessageReceived(MessageReceivedEvent e) {
                Hlaaftana.messages.put(e.getMessageId(), e.getMessage());
            }

            public void onMessageUpdate(MessageUpdateEvent e) {
                if (null != e.getGuild() && e.getGuild().getId().equals("145904657833787392") && Hlaaftana.messages.containsKey(e.getMessageId())) {
                    Message old = (Message)Hlaaftana.messages.get(e.getMessageId());
                    Hlaaftana.messages.put(e.getMessageId(), e.getMessage());
                    System.out.println("a");
                    Hlaaftana.client.getTextChannelById("325012587672764416").sendMessage(String.format("[#%s] %s (%s) edited: %s\nto: %s", e.getChannel().getName(), e.getAuthor().getName(), e.getAuthor().getId(), old.getContent(), e.getMessage().getContent())).queue();
                    System.out.println("b");
                }

            }

            public void onMessageDelete(MessageDeleteEvent e) {
                if (null != e.getGuild() && e.getGuild().getId().equals("145904657833787392") && Hlaaftana.messages.containsKey(e.getMessageId())) {
                    Message message = (Message)Hlaaftana.messages.get(e.getMessageId());
                    System.out.println("a");
                    Hlaaftana.client.getTextChannelById("325012587672764416").sendMessage(String.format("[#%s] %s (%s) deleted: %s", e.getChannel().getName(), message.getAuthor().getName(), message.getAuthor().getId(), message.getContent())).queue();
                    System.out.println("b");
                }

            }
        }}).buildBlocking();
    }
}
