package commands;

import java.awt.Color;
import java.io.File;
import java.time.Instant;
import java.util.List;

import org.json.simple.JSONObject;

import main.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import resources.Authenticator;
import resources.Database;

public class CharSelect {

	@SuppressWarnings("unchecked")
	public static void run(MessageReceivedEvent event, List<String> args) {
		System.out.println("\nEntered CharSelect Command!");
		
		if (event.getAuthor().isBot()) return;
		
		/*
		 * Grabs the prefix from auth.json.
		 * Gets message and channel for ease of access.
		 * Grabs the user database from users.json.
		 */
		Authenticator auth = new Authenticator();
		final String prefix = auth.getPrefix();
		
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();
		
		Database userDB;
		if (!App.DEV_MODE) userDB = new Database("users");
		else userDB = new Database("sampledb");
		JSONObject users = userDB.getDatabase();
		JSONObject user = (JSONObject) users.get(message.getAuthor().getId());
		JSONObject characters = (JSONObject) user.get("characters");
		JSONObject selected = (JSONObject) user.get("selected");
		
		/*
		 * First Pass, response with no arguments given.
		 */
		if (args.size() == 0 || args.get(0).equals("--here")) {
				
			/*
			 * Fills out basic embed fields.
			 * Fills out variable embed fields.
			 */
			final File file = new File("./assets/placeholders/placeholder-icon.png");
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Character Select");
			embed.setAuthor("Orion", null, event.getJDA().getSelfUser().getAvatarUrl());
			embed.setImage(null);
			embed.setThumbnail("attachment://placeholder-icon.png");
			embed.setTimestamp(Instant.now());
			
			String description = "Please re-enter this command with the corresponding number of the character you wish to select. "
					+ "(ex. **" + prefix + "charselect 2** to make your second listed character your selected character.)";
			embed.setDescription(description);
			
			/* 
			 * TODO: Order characters alphabetically or highest to lowest level.
			 * TODO: May need reactions to switch pages if a user has too many characters.
			 * Field generation for embed.
			 */
			String title = "";
			String field = "";
			int count = 1;
			JSONObject character = new JSONObject();
			for (Object nextChar : characters.values()) {
				character = (JSONObject) nextChar;
				title = count + ". **" + character.get("name") + "**";
				field = character.get("class") + " (Lv. " + character.get("level") + ")";
				embed.addField(title, field, false);
				++count;
			}
			
			/*
			 * Sets up the footer with the selected character (or "No Character") and color preference (or 0x1330c2).
			 * Populates the embed.
			 * Sends the menu to the channel it was requested from.
			 */
			String footer;
			if (!(selected.get("name") == null)) 
				footer = selected.get("name") + " (Lv. " + selected.get("level") + ") - " + selected.get("class");
			else footer = "No Character";
			
			Color prefColor;
			if (!(selected.get("color") == null))
				prefColor = new Color(Integer.decode((String) selected.get("color")));
			else prefColor = new Color(0x1330c2);
			
			embed.setFooter(footer);
			embed.setColor(prefColor);
			
			/*
			 * sends the menu in a dm to the requester.
			 */
			if (!(channel.getType() == ChannelType.PRIVATE) && args.size() == 0)
				channel.sendMessage("<@" + message.getAuthor().getId() + ">, Check your DMs.").queue();
			if (args.size() > 0 && args.get(0).equals("--here")) channel.sendMessage(embed.build()).addFile(file, "placeholder-icon.png").queue();
			else message.getAuthor().openPrivateChannel().queue(dm -> dm.sendMessage(embed.build()).addFile(file, "placeholder-icon.png").queue());
			return;
		}
		
		/*
		 * Second pass, select a different character with the second argument
		 */
		if (args.size() == 1) {
			//selected = characters.get(name);
			int count = 1;
			boolean charFound = false;
			JSONObject character = new JSONObject();
			for (Object nextChar : characters.values()) {
				character = (JSONObject) nextChar;
				if (args.get(0).equals(Integer.toString(count))) {
					charFound = true;
					break;
				}
				++count;
			}
			if (!charFound) {
				channel.sendMessage("That character number is invalid, please refer to the Character Select Menu for the appropriate character number and try again!").queue();
				return;
			}
			
			characters.put(selected.get("name"), selected);
			user.put("characters", characters);
			user.put("selected", character);
			
			if (!App.DEV_MODE) userDB.saveDatabase(users);
			else userDB.saveDatabase(users);
			
			channel.sendMessage(character.get("name") + " is now your selected character!").queue();
			return;
		}
		
		/*
		 * Too many arguments
		 */
		if (args.size() > 1) {
			channel.sendMessage("There are too many arguments in your command. Try again start with **" + prefix + "charselect** and follow the instructions and examples carefully.").queue();
			return;
		}
	}	
}