/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.actions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestMob;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.prompts.ItemStackPrompt;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;

public class ActionFactory implements ConversationAbandonedListener {

	private Quests plugin;
	private Map<UUID, Block> selectedExplosionLocations = new HashMap<UUID, Block>();
	private Map<UUID, Block> selectedEffectLocations = new HashMap<UUID, Block>();
	private Map<UUID, Block> selectedMobLocations = new HashMap<UUID, Block>();
	private Map<UUID, Block> selectedLightningLocations = new HashMap<UUID, Block>();
	private Map<UUID, Block> selectedTeleportLocations = new HashMap<UUID, Block>();
	private List<String> names = new LinkedList<String>();
	private ConversationFactory convoCreator;
	private File actionsFile;

	public ActionFactory(Quests plugin) {
		this.plugin = plugin;
		actionsFile = new File(plugin.getDataFolder(), "actions.yml");
		// Ensure to initialize convoCreator last so that 'this' is fully initialized before it is passed
		this.convoCreator = new ConversationFactory(plugin).withModality(false).withLocalEcho(false).withPrefix(new QuestCreatorPrefix()).withFirstPrompt(new MenuPrompt()).withTimeout(3600).thatExcludesNonPlayersWithMessage("Console may not perform this operation!").addConversationAbandonedListener(this);
	}
	
	public Map<UUID, Block> getSelectedExplosionLocations() {
		return selectedExplosionLocations;
	}

	public void setSelectedExplosionLocations(
			Map<UUID, Block> selectedExplosionLocations) {
		this.selectedExplosionLocations = selectedExplosionLocations;
	}

	public Map<UUID, Block> getSelectedEffectLocations() {
		return selectedEffectLocations;
	}

	public void setSelectedEffectLocations(Map<UUID, Block> selectedEffectLocations) {
		this.selectedEffectLocations = selectedEffectLocations;
	}

	public Map<UUID, Block> getSelectedMobLocations() {
		return selectedMobLocations;
	}

	public void setSelectedMobLocations(Map<UUID, Block> selectedMobLocations) {
		this.selectedMobLocations = selectedMobLocations;
	}

	public Map<UUID, Block> getSelectedLightningLocations() {
		return selectedLightningLocations;
	}

	public void setSelectedLightningLocations(
			Map<UUID, Block> selectedLightningLocations) {
		this.selectedLightningLocations = selectedLightningLocations;
	}

	public Map<UUID, Block> getSelectedTeleportLocations() {
		return selectedTeleportLocations;
	}

	public void setSelectedTeleportLocations(
			Map<UUID, Block> selectedTeleportLocations) {
		this.selectedTeleportLocations = selectedTeleportLocations;
	}

	public ConversationFactory getConversationFactory() {
		return convoCreator;
	}

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
		Player player = (Player) abandonedEvent.getContext().getForWhom();
		selectedExplosionLocations.remove(player.getUniqueId());
		selectedEffectLocations.remove(player.getUniqueId());
		selectedMobLocations.remove(player.getUniqueId());
		selectedLightningLocations.remove(player.getUniqueId());
		selectedTeleportLocations.remove(player.getUniqueId());
	}

	private class QuestCreatorPrefix implements ConversationPrefix {

		@Override
		public String getPrefix(ConversationContext context) {
			return "";
		}
	}

	private class MenuPrompt extends FixedSetPrompt {

		public MenuPrompt() {
			super("1", "2", "3", "4");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + Lang.get("eventEditorTitle") + "\n" + ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorCreate") + "\n" + ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorEdit") + "\n" + ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorDelete") + "\n" + ChatColor.GREEN + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("exit");
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			final Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase("1")) {
				if (player.hasPermission("quests.editor.actions.create") || player.hasPermission("quests.editor.events.create")) {
					context.setSessionData(CK.E_OLD_EVENT, "");
					return new ActionNamePrompt();
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
					return new MenuPrompt();
				}
			} else if (input.equalsIgnoreCase("2")) {
				if (player.hasPermission("quests.editor.actions.edit") || player.hasPermission("quests.editor.events.edit")) {
					if (plugin.getActions().isEmpty()) {
						((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorNoneToEdit"));
						return new MenuPrompt();
					} else {
						return new SelectEditPrompt();
					}
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
					return new MenuPrompt();
				}
			} else if (input.equalsIgnoreCase("3")) {
				if (player.hasPermission("quests.editor.actions.delete") || player.hasPermission("quests.editor.events.delete")) {
					if (plugin.getActions().isEmpty()) {
						((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorNoneToDelete"));
						return new MenuPrompt();
					} else {
						return new SelectDeletePrompt();
					}
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
					return new MenuPrompt();
				}
			} else if (input.equalsIgnoreCase("4")) {
				((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("exited"));
				return Prompt.END_OF_CONVERSATION;
			}
			return null;
		}
	}

	public Prompt returnToMenu() {
		return new CreateMenuPrompt();
	}

	public static void clearData(ConversationContext context) {
		context.setSessionData(CK.E_OLD_EVENT, null);
		context.setSessionData(CK.E_NAME, null);
		context.setSessionData(CK.E_MESSAGE, null);
		context.setSessionData(CK.E_CLEAR_INVENTORY, null);
		context.setSessionData(CK.E_FAIL_QUEST, null);
		context.setSessionData(CK.E_ITEMS, null);
		context.setSessionData(CK.E_ITEMS_AMOUNTS, null);
		context.setSessionData(CK.E_EXPLOSIONS, null);
		context.setSessionData(CK.E_EFFECTS, null);
		context.setSessionData(CK.E_EFFECTS_LOCATIONS, null);
		context.setSessionData(CK.E_WORLD_STORM, null);
		context.setSessionData(CK.E_WORLD_STORM_DURATION, null);
		context.setSessionData(CK.E_WORLD_THUNDER, null);
		context.setSessionData(CK.E_WORLD_THUNDER_DURATION, null);
		context.setSessionData(CK.E_MOB_TYPES, null);
		context.setSessionData(CK.E_LIGHTNING, null);
		context.setSessionData(CK.E_POTION_TYPES, null);
		context.setSessionData(CK.E_POTION_DURATIONS, null);
		context.setSessionData(CK.E_POTION_STRENGHT, null);
		context.setSessionData(CK.E_HUNGER, null);
		context.setSessionData(CK.E_SATURATION, null);
		context.setSessionData(CK.E_HEALTH, null);
		context.setSessionData(CK.E_TELEPORT, null);
		context.setSessionData(CK.E_COMMANDS, null);
		context.setSessionData(CK.E_TIMER, null);
		context.setSessionData(CK.E_CANCEL_TIMER, null);
	}

	public static void loadData(Action event, ConversationContext context) {
		if (event.message != null) {
			context.setSessionData(CK.E_MESSAGE, event.message);
		}
		if (event.clearInv == true) {
			context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
		} else {
			context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
		}
		if (event.failQuest == true) {
			context.setSessionData(CK.E_FAIL_QUEST, Lang.get("yesWord"));
		} else {
			context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
		}
		if (event.items != null && event.items.isEmpty() == false) {
			LinkedList<ItemStack> items = new LinkedList<ItemStack>();
			items.addAll(event.items);
			context.setSessionData(CK.E_ITEMS, items);
		}
		if (event.explosions != null && event.explosions.isEmpty() == false) {
			LinkedList<String> locs = new LinkedList<String>();
			for (Location loc : event.explosions) {
				locs.add(Quests.getLocationInfo(loc));
			}
			context.setSessionData(CK.E_EXPLOSIONS, locs);
		}
		if (event.effects != null && event.effects.isEmpty() == false) {
			LinkedList<String> locs = new LinkedList<String>();
			LinkedList<String> effs = new LinkedList<String>();
			for (Entry<Location, Effect> e : event.effects.entrySet()) {
				locs.add(Quests.getLocationInfo((Location) e.getKey()));
				effs.add(((Effect) e.getValue()).toString());
			}
			context.setSessionData(CK.E_EFFECTS, effs);
			context.setSessionData(CK.E_EFFECTS_LOCATIONS, locs);
		}
		if (event.stormWorld != null) {
			context.setSessionData(CK.E_WORLD_STORM, event.stormWorld.getName());
			context.setSessionData(CK.E_WORLD_STORM_DURATION, (int) event.stormDuration);
		}
		if (event.thunderWorld != null) {
			context.setSessionData(CK.E_WORLD_THUNDER, event.thunderWorld.getName());
			context.setSessionData(CK.E_WORLD_THUNDER_DURATION, (int) event.thunderDuration);
		}
		if (event.mobSpawns != null && event.mobSpawns.isEmpty() == false) {
			LinkedList<String> questMobs = new LinkedList<String>();
			for (QuestMob questMob : event.mobSpawns) {
				questMobs.add(questMob.serialize());
			}
			context.setSessionData(CK.E_MOB_TYPES, questMobs);
		}
		if (event.lightningStrikes != null && event.lightningStrikes.isEmpty() == false) {
			LinkedList<String> locs = new LinkedList<String>();
			for (Location loc : event.lightningStrikes) {
				locs.add(Quests.getLocationInfo(loc));
			}
			context.setSessionData(CK.E_LIGHTNING, locs);
		}
		if (event.potionEffects != null && event.potionEffects.isEmpty() == false) {
			LinkedList<String> types = new LinkedList<String>();
			LinkedList<Long> durations = new LinkedList<Long>();
			LinkedList<Integer> mags = new LinkedList<Integer>();
			for (PotionEffect pe : event.potionEffects) {
				types.add(pe.getType().getName());
				durations.add((long) pe.getDuration());
				mags.add(pe.getAmplifier());
			}
			context.setSessionData(CK.E_POTION_TYPES, types);
			context.setSessionData(CK.E_POTION_DURATIONS, durations);
			context.setSessionData(CK.E_POTION_STRENGHT, mags);
		}
		if (event.hunger > -1) {
			context.setSessionData(CK.E_HUNGER, (Integer) event.hunger);
		}
		if (event.saturation > -1) {
			context.setSessionData(CK.E_SATURATION, (Integer) event.saturation);
		}
		if (event.health > -1) {
			context.setSessionData(CK.E_HEALTH, (Float) event.health);
		}
		if (event.teleport != null) {
			context.setSessionData(CK.E_TELEPORT, Quests.getLocationInfo(event.teleport));
		}
		if (event.commands != null) {
			context.setSessionData(CK.E_COMMANDS, event.commands);
		}
		if (event.timer > 0) {
			context.setSessionData(CK.E_TIMER, event.timer);
		}
		if (event.cancelTimer) {
			context.setSessionData(CK.E_CANCEL_TIMER, true);
		}
	}

	private class SelectEditPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("eventEditorEdit") + " -\n";
			for (Action a : plugin.getActions()) {
				text += ChatColor.AQUA + a.getName() + ChatColor.YELLOW + ", ";
			}
			text = text.substring(0, text.length() - 2) + "\n";
			text += ChatColor.YELLOW + Lang.get("eventEditorEnterEventName");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				Action a = plugin.getAction(input);
				if (a != null) {
					context.setSessionData(CK.E_OLD_EVENT, a.getName());
					context.setSessionData(CK.E_NAME, a.getName());
					loadData(a, context);
					return new CreateMenuPrompt();
				}
				((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorNotFound"));
				return new SelectEditPrompt();
			} else {
				return new MenuPrompt();
			}
		}
	}

	private class SelectDeletePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("eventEditorDelete") + " -\n";
			for (Action a : plugin.getActions()) {
				text += ChatColor.AQUA + a.getName() + ChatColor.YELLOW + ",";
			}
			text = text.substring(0, text.length() - 1) + "\n";
			text += ChatColor.YELLOW + Lang.get("eventEditorEnterEventName");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				LinkedList<String> used = new LinkedList<String>();
				Action a = plugin.getAction(input);
				if (a != null) {
					for (Quest quest : plugin.getQuests()) {
						for (Stage stage : quest.getStages()) {
							if (stage.getFinishEvent() != null && stage.getFinishEvent().getName().equalsIgnoreCase(a.getName())) {
								used.add(quest.getName());
								break;
							}
						}
					}
					if (used.isEmpty()) {
						context.setSessionData(CK.ED_EVENT_DELETE, a.getName());
						return new DeletePrompt();
					} else {
						((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorEventInUse") + " \"" + ChatColor.DARK_PURPLE + a.getName() + ChatColor.RED + "\":");
						for (String s : used) {
							((Player) context.getForWhom()).sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
						}
						((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorMustModifyQuests"));
						return new SelectDeletePrompt();
					}
				}
				((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorNotFound"));
				return new SelectDeletePrompt();
			} else {
				return new MenuPrompt();
			}
		}
	}

	private class DeletePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " + Lang.get("yesWord") + "\n";
			text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " + Lang.get("noWord");
			return ChatColor.RED + Lang.get("eventEditorDeletePrompt") + " (" + ChatColor.YELLOW + (String) context.getSessionData(CK.ED_EVENT_DELETE) + ChatColor.RED + ")\n" + text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
				deleteAction(context);
				return new MenuPrompt();
			} else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
				return new MenuPrompt();
			} else {
				return new DeletePrompt();
			}
		}
	}

	private class CreateMenuPrompt extends FixedSetPrompt {

		public CreateMenuPrompt() {
			super("1", "2", "3", "4", "5", "6", "7", "8", "9");
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("event") + ": " + ChatColor.AQUA + context.getSessionData(CK.E_NAME) + ChatColor.GOLD + " -\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetName") + "\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GOLD + " - " + Lang.get("eventEditorPlayer") + "\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GOLD + " - " + Lang.get("eventEditorTimer") +"\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.GOLD + " - " + Lang.get("eventEditorEffect") +"\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.GOLD + " - " + Lang.get("eventEditorWeather") +"\n";
			if (context.getSessionData(CK.E_MOB_TYPES) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobSpawns") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobSpawns") + "\n";
				for (String s : types) {
					QuestMob qm = QuestMob.fromString(s);
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + qm.getType().name() + ((qm.getName() != null) ? ": " + qm.getName() : "") + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + qm.getSpawnAmounts() + ChatColor.GRAY + " -> " + ChatColor.GREEN + Quests.getLocationInfo(qm.getSpawnLocation()) + "\n";
				}
			}
			if (context.getSessionData(CK.E_FAIL_QUEST) == null) {
				context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
			}
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorFailQuest") + ": " + ChatColor.AQUA + context.getSessionData(CK.E_FAIL_QUEST) + "\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "8" + ChatColor.RESET + ChatColor.GREEN + " - " + Lang.get("save") + "\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "9" + ChatColor.RESET + ChatColor.RED + " - " + Lang.get("exit");
			return text;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new SetNamePrompt();
			} else if (input.equalsIgnoreCase("2")) {
				return new PlayerPrompt();
			} else if (input.equalsIgnoreCase("3")) {
				return new TimerPrompt();
			} else if (input.equalsIgnoreCase("4")) {
				return new EffectPrompt();
			} else if (input.equalsIgnoreCase("5")) {
				return new WeatherPrompt();
			} else if (input.equalsIgnoreCase("6")) {
				return new MobPrompt();
			} else if (input.equalsIgnoreCase("7")) {
				String s = (String) context.getSessionData(CK.E_FAIL_QUEST);
				if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
					context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
				} else {
					context.setSessionData(CK.E_FAIL_QUEST, Lang.get("yesWord"));
				}
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase("8")) {
				if (context.getSessionData(CK.E_OLD_EVENT) != null) {
					return new SavePrompt((String) context.getSessionData(CK.E_OLD_EVENT));
				} else {
					return new SavePrompt(null);
				}
			} else if (input.equalsIgnoreCase("9")) {
				return new ExitPrompt();
			}
			return null;
		}
	}
	
	private class PlayerPrompt extends FixedSetPrompt {
		
		public PlayerPrompt() {
			super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("eventEditorPlayer") + " -\n";
			if (context.getSessionData(CK.E_MESSAGE) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMessage") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMessage") + " (" + ChatColor.AQUA + context.getSessionData(CK.E_MESSAGE) + ChatColor.RESET + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(CK.E_CLEAR_INVENTORY) == null) {
				context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
			}
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorClearInv") + ": " + ChatColor.AQUA + context.getSessionData(CK.E_CLEAR_INVENTORY) + "\n";
			if (context.getSessionData(CK.E_ITEMS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetItems") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetItems") + "\n";
				LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.E_ITEMS);
				for (ItemStack is : items) {
					if (is != null) {
						text += ChatColor.GRAY + "    - " + ItemUtil.getString(is) + "\n";
					}
				}
			}
			if (context.getSessionData(CK.E_POTION_TYPES) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + "\n";
				LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES);
				LinkedList<Long> durations = (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS);
				LinkedList<Integer> mags = (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGHT);
				int index = -1;
				for (String type : types) {
					index++;
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + type + ChatColor.DARK_PURPLE + " " + RomanNumeral.getNumeral(mags.get(index)) + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA + Quests.getTime(durations.get(index) * 50L) + "\n";
				}
			}
			if (context.getSessionData(CK.E_HUNGER) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetHunger") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetHunger") + ChatColor.AQUA + " (" + (Integer) context.getSessionData(CK.E_HUNGER) + ")\n";
			}
			if (context.getSessionData(CK.E_SATURATION) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetSaturation") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetSaturation") + ChatColor.AQUA + " (" + (Integer) context.getSessionData(CK.E_SATURATION) + ")\n";
			}
			if (context.getSessionData(CK.E_HEALTH) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetHealth") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetHealth") + ChatColor.AQUA + " (" + (Integer) context.getSessionData(CK.E_HEALTH) + ")\n";
			}
			if (context.getSessionData(CK.E_TELEPORT) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "8" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetTeleport") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "8" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetTeleport") + ChatColor.AQUA + " (" + (String) context.getSessionData(CK.E_TELEPORT) + ")\n";
			}
			if (context.getSessionData(CK.E_COMMANDS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "9" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetCommands") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "9" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetCommands") + "\n";
				for (String s : (LinkedList<String>) context.getSessionData(CK.E_COMMANDS)) {
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
				}
			}
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "10 " + ChatColor.RESET + ChatColor.YELLOW + "- " + Lang.get("done") + "\n";
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new MessagePrompt();
			} else if (input.equalsIgnoreCase("2")) {
				String s = (String) context.getSessionData(CK.E_CLEAR_INVENTORY);
				if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
					context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
				} else {
					context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
				}
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase("3")) {
				return new ItemListPrompt();
			} else if (input.equalsIgnoreCase("4")) {
				return new PotionEffectPrompt();
			} else if (input.equalsIgnoreCase("5")) {
				return new HungerPrompt();
			} else if (input.equalsIgnoreCase("6")) {
				return new SaturationPrompt();
			} else if (input.equalsIgnoreCase("7")) {
				return new HealthPrompt();
			} else if (input.equalsIgnoreCase("8")) {
				selectedTeleportLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
				return new TeleportPrompt();
			} else if (input.equalsIgnoreCase("9")) {
				return new CommandsPrompt();
			}
			return new CreateMenuPrompt();
		}
	}
	
	private class TimerPrompt extends FixedSetPrompt {
		
		public TimerPrompt() {
			super("1", "2", "3");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("eventEditorTimer") + " -\n";
			if (context.getSessionData(CK.E_TIMER) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetTimer") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetTimer") + "(" + ChatColor.AQUA + "\"" + context.getSessionData(CK.E_TIMER) + "\"" + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(CK.E_CANCEL_TIMER) == null) {
				context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("noWord"));
			}
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorCancelTimer") + ": " + ChatColor.AQUA + context.getSessionData(CK.E_CANCEL_TIMER) + "\n";
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.YELLOW + "- " + Lang.get("done") + "\n";
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new FailTimerPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				String s = (String) context.getSessionData(CK.E_CANCEL_TIMER);
				if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
					context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("noWord"));
				} else {
					context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("yesWord"));
				}
				return new CreateMenuPrompt();
			}
			return new CreateMenuPrompt();
		}
	}
	
	private class EffectPrompt extends FixedSetPrompt {
		
		public EffectPrompt() {
			super("1", "2", "3");
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("eventEditorEffect") + " -\n";
			if (context.getSessionData(CK.E_EFFECTS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetEffects") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetEffects") + "\n";
				LinkedList<String> effects = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS);
				LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
				for (String effect : effects) {
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + effect + ChatColor.GRAY + " at " + ChatColor.DARK_AQUA + locations.get(effects.indexOf(effect)) + "\n";
				}
			}
			if (context.getSessionData(CK.E_EXPLOSIONS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetExplosions") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetExplosions") + "\n";
				LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS);
				for (String loc : locations) {
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + loc + "\n";
				}
			}
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.YELLOW + "- " + Lang.get("done") + "\n";
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new SoundEffectListPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				selectedExplosionLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
				return new ExplosionPrompt();
			}
			return new CreateMenuPrompt();
		}
	}
	
	private class WeatherPrompt extends FixedSetPrompt {
		
		public WeatherPrompt() {
			super("1", "2", "3", "4");
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("eventEditorWeather") + " -\n";
			if (context.getSessionData(CK.E_WORLD_STORM) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetStorm") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetStorm") + " (" + ChatColor.AQUA + (String) context.getSessionData(CK.E_WORLD_STORM) + ChatColor.YELLOW + " -> " + ChatColor.DARK_AQUA + Quests.getTime(Long.valueOf((int)context.getSessionData(CK.E_WORLD_STORM_DURATION) * 1000)) + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetThunder") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetThunder") + " (" + ChatColor.AQUA + (String) context.getSessionData(CK.E_WORLD_THUNDER) + ChatColor.YELLOW + " -> " + ChatColor.DARK_AQUA + Quests.getTime(Long.valueOf((int)context.getSessionData(CK.E_WORLD_THUNDER_DURATION) * 1000)) + ChatColor.YELLOW + ")\n";
			}
			
			if (context.getSessionData(CK.E_LIGHTNING) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetLightning") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetLightning") + "\n";
				LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);
				for (String loc : locations) {
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + loc + "\n";
				}
			}
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.GREEN + "- " + Lang.get("done") + "\n";
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new StormPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				return new ThunderPrompt();
			} else if (input.equalsIgnoreCase("3")) {
				selectedLightningLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
				return new LightningPrompt();
			}
			return new CreateMenuPrompt();
		}
	}

	private class FailTimerPrompt extends NumericPrompt {

		@Override
		protected Prompt acceptValidatedInput(final ConversationContext context, final Number number) {
			context.setSessionData(CK.E_TIMER, number);
			return new CreateMenuPrompt();
		}

		@Override
		public String getPromptText(final ConversationContext conversationContext) {
			return ChatColor.YELLOW + Lang.get("eventEditorEnterTimerSeconds");
		}
	}

	private class SavePrompt extends StringPrompt {

		String modName = null;
		LinkedList<String> modified = new LinkedList<String>();

		public SavePrompt(String modifiedName) {
			if (modifiedName != null) {
				modName = modifiedName;
				for (Quest q : plugin.getQuests()) {
					for (Stage s : q.getStages()) {
						if (s.getFinishEvent() != null && s.getFinishEvent().getName() != null) {
							if (s.getFinishEvent().getName().equalsIgnoreCase(modifiedName)) {
								modified.add(q.getName());
								break;
							}
						}
					}
				}
			}
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.YELLOW + Lang.get("questEditorSave") + " \"" + ChatColor.AQUA + context.getSessionData(CK.E_NAME) + ChatColor.YELLOW + "\"?\n";
			if (modified.isEmpty() == false) {
				text += ChatColor.RED + Lang.get("eventEditorModifiedNote") + "\n";
				for (String s : modified) {
					text += ChatColor.GRAY + "    - " + ChatColor.DARK_RED + s + "\n";
				}
				text += ChatColor.RED + Lang.get("eventEditorForcedToQuit") + "\n";
			}
			return text + ChatColor.GREEN + "1 - " + Lang.get("yesWord") + "\n" + "2 - " + Lang.get("noWord");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
				saveAction(context);
				return new MenuPrompt();
			} else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
				return new CreateMenuPrompt();
			} else {
				return new SavePrompt(modName);
			}
		}
	}
	
	private class ExitPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GREEN + "" +  ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.GREEN + " - " + Lang.get("yesWord") + "\n" 
					+ ChatColor.RED + "" +  ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.RED + " - " + Lang.get("noWord");
			return ChatColor.YELLOW + Lang.get("questEditorExited") + "\n" + text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
				context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + Lang.get("exited"));
				clearData(context);
				return Prompt.END_OF_CONVERSATION;
			} else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
				return new CreateMenuPrompt();
			} else {
				return new ExitPrompt();
			}
		}
	}

	// Convenience methods to reduce typecasting
	private static String getCString(ConversationContext context, String path) {
		return (String) context.getSessionData(path);
	}

	@SuppressWarnings("unchecked")
	private static LinkedList<String> getCStringList(ConversationContext context, String path) {
		return (LinkedList<String>) context.getSessionData(path);
	}

	private static Integer getCInt(ConversationContext context, String path) {
		return (Integer) context.getSessionData(path);
	}

	@SuppressWarnings("unchecked")
	private static LinkedList<Integer> getCIntList(ConversationContext context, String path) {
		return (LinkedList<Integer>) context.getSessionData(path);
	}

	@SuppressWarnings("unused")
	private static Boolean getCBoolean(ConversationContext context, String path) {
		return (Boolean) context.getSessionData(path);
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private static LinkedList<Boolean> getCBooleanList(ConversationContext context, String path) {
		return (LinkedList<Boolean>) context.getSessionData(path);
	}

	@SuppressWarnings({ "unused" })
	private static Long getCLong(ConversationContext context, String path) {
		return (Long) context.getSessionData(path);
	}

	@SuppressWarnings("unchecked")
	private static LinkedList<Long> getCLongList(ConversationContext context, String path) {
		return (LinkedList<Long>) context.getSessionData(path);
	}
	//

	private void deleteAction(ConversationContext context) {
		YamlConfiguration data = new YamlConfiguration();
		try {
			actionsFile = new File(plugin.getDataFolder(), "actions.yml");
			data.load(actionsFile);
		} catch (IOException e) {
			e.printStackTrace();
			((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
			return;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
			return;
		}
		String event = (String) context.getSessionData(CK.ED_EVENT_DELETE);
		ConfigurationSection sec = data.getConfigurationSection("events");
		sec.set(event, null);
		try {
			data.save(actionsFile);
		} catch (IOException e) {
			((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
			return;
		}
		plugin.reloadQuests();
		((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorDeleted"));
		for (Quester q : plugin.getQuesters()) {
			for (Quest quest : q.getCurrentQuests().keySet()) {
				q.checkQuest(quest);
			}
		}
		clearData(context);
	}

	private void saveAction(ConversationContext context) {
		YamlConfiguration data = new YamlConfiguration();
		try {
			actionsFile = new File(plugin.getDataFolder(), "actions.yml");
			data.load(actionsFile);
		} catch (IOException e) {
			e.printStackTrace();
			((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
			return;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
			return;
		}
		String key = "actions";
		ConfigurationSection sec = data.getConfigurationSection(key);
		if (sec == null) {
			key = "events";
			sec = data.getConfigurationSection(key);
		}
		if (((String) context.getSessionData(CK.E_OLD_EVENT)).isEmpty() == false) {
			data.set(key + "." + (String) context.getSessionData(CK.E_OLD_EVENT), null);
			LinkedList<Action> temp = plugin.getActions();
			temp.remove(plugin.getAction((String) context.getSessionData(CK.E_OLD_EVENT)));
			plugin.setActions(temp);
		}
		ConfigurationSection section = data.createSection(key + "." + (String) context.getSessionData(CK.E_NAME));
		names.remove((String) context.getSessionData(CK.E_NAME));
		if (context.getSessionData(CK.E_MESSAGE) != null) {
			section.set("message", getCString(context, CK.E_MESSAGE));
		}
		if (context.getSessionData(CK.E_CLEAR_INVENTORY) != null) {
			String s = getCString(context, CK.E_CLEAR_INVENTORY);
			if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
				section.set("clear-inventory", true);
			}
		}
		if (context.getSessionData(CK.E_FAIL_QUEST) != null) {
			String s = getCString(context, CK.E_FAIL_QUEST);
			if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
				section.set("fail-quest", true);
			}
		}
		if (context.getSessionData(CK.E_ITEMS) != null) {
			@SuppressWarnings("unchecked")
			LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.E_ITEMS);
			section.set("items", items);
		}
		if (context.getSessionData(CK.E_EXPLOSIONS) != null) {
			LinkedList<String> locations = getCStringList(context, CK.E_EXPLOSIONS);
			section.set("explosions", locations);
		}
		if (context.getSessionData(CK.E_EFFECTS) != null) {
			LinkedList<String> effects = getCStringList(context, CK.E_EFFECTS);
			LinkedList<String> locations = getCStringList(context, CK.E_EFFECTS_LOCATIONS);
			section.set("effects", effects);
			section.set("effect-locations", locations);
		}
		if (context.getSessionData(CK.E_WORLD_STORM) != null) {
			String world = getCString(context, CK.E_WORLD_STORM);
			int duration = getCInt(context, CK.E_WORLD_STORM_DURATION);
			section.set("storm-world", world);
			section.set("storm-duration", duration);
		}
		if (context.getSessionData(CK.E_WORLD_THUNDER) != null) {
			String world = getCString(context, CK.E_WORLD_THUNDER);
			int duration = getCInt(context, CK.E_WORLD_THUNDER_DURATION);
			section.set("thunder-world", world);
			section.set("thunder-duration", duration);
		}
		try {
			if (context.getSessionData(CK.E_MOB_TYPES) != null) {
				int count = 0;
				for (String s : getCStringList(context, CK.E_MOB_TYPES)) {
					ConfigurationSection ss = section.getConfigurationSection("mob-spawns." + count);
					if (ss == null) {
						ss = section.createSection("mob-spawns." + count);
					}
					QuestMob questMob = QuestMob.fromString(s);
					if (questMob == null) {
						continue;
					}
					ss.set("name", questMob.getName());
					ss.set("spawn-location", Quests.getLocationInfo(questMob.getSpawnLocation()));
					ss.set("mob-type", questMob.getType().name());
					ss.set("spawn-amounts", questMob.getSpawnAmounts());
					ss.set("held-item", ItemUtil.serializeItemStack(questMob.getInventory()[0]));
					ss.set("held-item-drop-chance", questMob.getDropChances()[0]);
					ss.set("boots", ItemUtil.serializeItemStack(questMob.getInventory()[1]));
					ss.set("boots-drop-chance", questMob.getDropChances()[1]);
					ss.set("leggings", ItemUtil.serializeItemStack(questMob.getInventory()[2]));
					ss.set("leggings-drop-chance", questMob.getDropChances()[2]);
					ss.set("chest-plate", ItemUtil.serializeItemStack(questMob.getInventory()[3]));
					ss.set("chest-plate-drop-chance", questMob.getDropChances()[3]);
					ss.set("helmet", ItemUtil.serializeItemStack(questMob.getInventory()[4]));
					ss.set("helmet-drop-chance", questMob.getDropChances()[4]);
					count++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (context.getSessionData(CK.E_LIGHTNING) != null) {
			LinkedList<String> locations = getCStringList(context, CK.E_LIGHTNING);
			section.set("lightning-strikes", locations);
		}
		if (context.getSessionData(CK.E_COMMANDS) != null) {
			LinkedList<String> commands = getCStringList(context, CK.E_COMMANDS);
			if (commands.isEmpty() == false) {
				section.set("commands", commands);
			}
		}
		if (context.getSessionData(CK.E_POTION_TYPES) != null) {
			LinkedList<String> types = getCStringList(context, CK.E_POTION_TYPES);
			LinkedList<Long> durations = getCLongList(context, CK.E_POTION_DURATIONS);
			LinkedList<Integer> mags = getCIntList(context, CK.E_POTION_STRENGHT);
			section.set("potion-effect-types", types);
			section.set("potion-effect-durations", durations);
			section.set("potion-effect-amplifiers", mags);
		}
		if (context.getSessionData(CK.E_HUNGER) != null) {
			Integer i = getCInt(context, CK.E_HUNGER);
			section.set("hunger", i);
		}
		if (context.getSessionData(CK.E_SATURATION) != null) {
			Integer i = getCInt(context, CK.E_SATURATION);
			section.set("saturation", i);
		}
		if (context.getSessionData(CK.E_HEALTH) != null) {
			Integer i = getCInt(context, CK.E_HEALTH);
			section.set("health", i);
		}
		if (context.getSessionData(CK.E_TELEPORT) != null) {
			section.set("teleport-location", getCString(context, CK.E_TELEPORT));
		}
		if (context.getSessionData(CK.E_TIMER) != null && (int) context.getSessionData(CK.E_TIMER) > 0) {
			section.set("timer", getCInt(context, CK.E_TIMER));
		}
		if (context.getSessionData(CK.E_CANCEL_TIMER) != null) {
			String s = getCString(context, CK.E_CANCEL_TIMER);
			if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
				section.set("cancel-timer", true);
			}
		}
		try {
			data.save(actionsFile);
		} catch (IOException e) {
			((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
			return;
		}
		plugin.reloadQuests();
		((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorSaved"));
		for (Quester q : plugin.getQuesters()) {
			for (Quest quest : q.getCurrentQuests().keySet()) {
				q.checkQuest(quest);
			}
		}
		clearData(context);
	}

	private class ActionNamePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.AQUA + Lang.get("eventEditorCreate") + ChatColor.GOLD + " - " 
					+ Lang.get("eventEditorEnterEventName");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				for (Action e : plugin.getActions()) {
					if (e.getName().equalsIgnoreCase(input)) {
						context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorExists"));
						return new ActionNamePrompt();
					}
				}
				if (names.contains(input)) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSomeone"));
					return new ActionNamePrompt();
				}
				if (StringUtils.isAlphanumeric(input) == false) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorAlpha"));
					return new ActionNamePrompt();
				}
				context.setSessionData(CK.E_NAME, input);
				names.add(input);
				return new CreateMenuPrompt();
			} else {
				return new MenuPrompt();
			}
		}
	}

	private class ExplosionPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorExplosionPrompt");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
				Block block = selectedExplosionLocations.get(player.getUniqueId());
				if (block != null) {
					Location loc = block.getLocation();
					LinkedList<String> locs;
					if (context.getSessionData(CK.E_EXPLOSIONS) != null) {
						locs = (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS);
					} else {
						locs = new LinkedList<String>();
					}
					locs.add(Quests.getLocationInfo(loc));
					context.setSessionData(CK.E_EXPLOSIONS, locs);
					selectedExplosionLocations.remove(player.getUniqueId());
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
					return new ExplosionPrompt();
				}
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.E_EXPLOSIONS, null);
				selectedExplosionLocations.remove(player.getUniqueId());
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				selectedExplosionLocations.remove(player.getUniqueId());
				return new CreateMenuPrompt();
			} else {
				return new ExplosionPrompt();
			}
		}
	}

	private class SetNamePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorEnterEventName");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				for (Action e : plugin.getActions()) {
					if (e.getName().equalsIgnoreCase(input)) {
						context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorExists"));
						return new SetNamePrompt();
					}
				}
				if (names.contains(input)) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSomeone"));
					return new SetNamePrompt();
				}
				if (StringUtils.isAlphanumeric(input) == false) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorAlpha"));
					return new SetNamePrompt();
				}
				names.remove((String) context.getSessionData(CK.E_NAME));
				context.setSessionData(CK.E_NAME, input);
				names.add(input);
			}
			return new CreateMenuPrompt();
		}
	}

	private class MessagePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetMessagePrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				context.setSessionData(CK.E_MESSAGE, input);
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.E_MESSAGE, null);
			}
			return new CreateMenuPrompt();
		}
	}

	private class ItemListPrompt extends FixedSetPrompt {

		public ItemListPrompt() {
			super("1", "2", "3");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			// Check/add newly made item
			if (context.getSessionData("newItem") != null) {
				if (context.getSessionData(CK.E_ITEMS) != null) {
					List<ItemStack> items = getItems(context);
					items.add((ItemStack) context.getSessionData("tempStack"));
					context.setSessionData(CK.E_ITEMS, items);
				} else {
					LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
					itemRews.add((ItemStack) context.getSessionData("tempStack"));
					context.setSessionData(CK.E_ITEMS, itemRews);
				}
				context.setSessionData("newItem", null);
				context.setSessionData("tempStack", null);
			}
			String text = ChatColor.GOLD + Lang.get("eventEditorGiveItemsTitle") + "\n";
			if (context.getSessionData(CK.E_ITEMS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddItem") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				for (ItemStack is : getItems(context)) {
					text += ChatColor.GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddItem") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new ItemStackPrompt(ItemListPrompt.this);
			} else if (input.equalsIgnoreCase("2")) {
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorItemsCleared"));
				context.setSessionData(CK.E_ITEMS, null);
				return new ItemListPrompt();
			} else if (input.equalsIgnoreCase("3")) {
				return new CreateMenuPrompt();
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		private List<ItemStack> getItems(ConversationContext context) {
			return (List<ItemStack>) context.getSessionData(CK.E_ITEMS);
		}
	}

	private class SoundEffectListPrompt extends FixedSetPrompt {

		public SoundEffectListPrompt() {
			super("1", "2", "3", "4");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("eventEditorEffects") + " -\n";
			if (context.getSessionData(CK.E_EFFECTS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddEffect") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.GRAY + "2 - " + Lang.get("eventEditorAddEffectLocation") + " (" + Lang.get("eventEditorNoEffects") + ")\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddEffect") + "\n";
				for (String s : getEffects(context)) {
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
				}
				if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) == null) {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddEffectLocation") + " (" + Lang.get("noneSet") + ")\n";
				} else {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddEffectLocation") + "\n";
					for (String s : getEffectLocations(context)) {
						text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
					}
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new SoundEffectPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				if (context.getSessionData(CK.E_EFFECTS) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustAddEffects"));
					return new SoundEffectListPrompt();
				} else {
					selectedEffectLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
					return new SoundEffectLocationPrompt();
				}
			} else if (input.equalsIgnoreCase("3")) {
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorEffectsCleared"));
				context.setSessionData(CK.E_EFFECTS, null);
				context.setSessionData(CK.E_EFFECTS_LOCATIONS, null);
				return new SoundEffectListPrompt();
			} else if (input.equalsIgnoreCase("4")) {
				int one;
				int two;
				if (context.getSessionData(CK.E_EFFECTS) != null) {
					one = getEffects(context).size();
				} else {
					one = 0;
				}
				if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) != null) {
					two = getEffectLocations(context).size();
				} else {
					two = 0;
				}
				if (one == two) {
					return new CreateMenuPrompt();
				} else {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
					return new SoundEffectListPrompt();
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		private List<String> getEffects(ConversationContext context) {
			return (List<String>) context.getSessionData(CK.E_EFFECTS);
		}

		@SuppressWarnings("unchecked")
		private List<String> getEffectLocations(ConversationContext context) {
			return (List<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
		}
	}

	private class SoundEffectLocationPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorEffectLocationPrompt");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
				Block block = selectedEffectLocations.get(player.getUniqueId());
				if (block != null) {
					Location loc = block.getLocation();
					LinkedList<String> locs;
					if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) != null) {
						locs = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
					} else {
						locs = new LinkedList<String>();
					}
					locs.add(Quests.getLocationInfo(loc));
					context.setSessionData(CK.E_EFFECTS_LOCATIONS, locs);
					selectedEffectLocations.remove(player.getUniqueId());
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
					return new SoundEffectLocationPrompt();
				}
				return new SoundEffectListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				selectedEffectLocations.remove(player.getUniqueId());
				return new SoundEffectListPrompt();
			} else {
				return new SoundEffectLocationPrompt();
			}
		}
	}

	private class SoundEffectPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String effects = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorEffectsTitle") + "\n";
			effects += ChatColor.DARK_PURPLE + "BLAZE_SHOOT " + ChatColor.GRAY + "- " + Lang.get("effBlazeShoot") + "\n";
			effects += ChatColor.DARK_PURPLE + "BOW_FIRE " + ChatColor.GRAY + "- " + Lang.get("effBowFire") + "\n";
			effects += ChatColor.DARK_PURPLE + "CLICK1 " + ChatColor.GRAY + "- " + Lang.get("effClick1") + "\n";
			effects += ChatColor.DARK_PURPLE + "CLICK2 " + ChatColor.GRAY + "- " + Lang.get("effClick2") + "\n";
			effects += ChatColor.DARK_PURPLE + "DOOR_TOGGLE " + ChatColor.GRAY + "- " + Lang.get("effDoorToggle") + "\n";
			effects += ChatColor.DARK_PURPLE + "EXTINGUISH " + ChatColor.GRAY + "- " + Lang.get("effExtinguish") + "\n";
			effects += ChatColor.DARK_PURPLE + "GHAST_SHOOT " + ChatColor.GRAY + "- " + Lang.get("effGhastShoot") + "\n";
			effects += ChatColor.DARK_PURPLE + "GHAST_SHRIEK " + ChatColor.GRAY + "- " + Lang.get("effGhastShriek") + "\n";
			effects += ChatColor.DARK_PURPLE + "ZOMBIE_CHEW_IRON_DOOR " + ChatColor.GRAY + "- " + Lang.get("effZombieWood") + "\n";
			effects += ChatColor.DARK_PURPLE + "ZOMBIE_CHEW_WOODEN_DOOR " + ChatColor.GRAY + "- " + Lang.get("effZombieIron") + "\n";
			return ChatColor.YELLOW + effects + Lang.get("effEnterName");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (Quests.getEffect(input.toUpperCase()) != null) {
					LinkedList<String> effects;
					if (context.getSessionData(CK.E_EFFECTS) != null) {
						effects = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS);
					} else {
						effects = new LinkedList<String>();
					}
					effects.add(input.toUpperCase());
					context.setSessionData(CK.E_EFFECTS, effects);
					selectedEffectLocations.remove(player.getUniqueId());
					return new SoundEffectListPrompt();
				} else {
					player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("eventEditorInvalidEffect"));
					return new SoundEffectPrompt();
				}
			} else {
				selectedEffectLocations.remove(player.getUniqueId());
				return new SoundEffectListPrompt();
			}
		}
	}

	private class StormPrompt extends FixedSetPrompt {

		public StormPrompt() {
			super("1", "2", "3", "4");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + Lang.get("eventEditorStormTitle") + "\n";
			if (context.getSessionData(CK.E_WORLD_STORM) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.GRAY + "2 - " + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + ChatColor.AQUA + ((String) context.getSessionData(CK.E_WORLD_STORM)) + ChatColor.YELLOW + ")\n";
				if (context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
				} else {
					int dur = (int) context.getSessionData(CK.E_WORLD_STORM_DURATION);
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + ChatColor.AQUA + Quests.getTime(dur * 1000) + ChatColor.YELLOW + ")\n";
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new StormWorldPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				if (context.getSessionData(CK.E_WORLD_STORM) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSetWorldFirst"));
					return new StormPrompt();
				} else {
					return new StormDurationPrompt();
				}
			} else if (input.equalsIgnoreCase("3")) {
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorStormCleared"));
				context.setSessionData(CK.E_WORLD_STORM, null);
				context.setSessionData(CK.E_WORLD_STORM_DURATION, null);
				return new StormPrompt();
			} else if (input.equalsIgnoreCase("4")) {
				if (context.getSessionData(CK.E_WORLD_STORM) != null && context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetStormDuration"));
					return new StormPrompt();
				} else {
					return new CreateMenuPrompt();
				}
			}
			return null;
		}
	}

	private class StormWorldPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String effects = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorWorldsTitle") + "\n" + ChatColor.DARK_PURPLE;
			for (World w : plugin.getServer().getWorlds()) {
				effects += w.getName() + ", ";
			}
			effects = effects.substring(0, effects.length());
			return ChatColor.YELLOW + effects + Lang.get("eventEditorEnterStormWorld");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (plugin.getServer().getWorld(input) != null) {
					context.setSessionData(CK.E_WORLD_STORM, plugin.getServer().getWorld(input).getName());
				} else {
					player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("eventEditorInvalidWorld"));
					return new StormWorldPrompt();
				}
			}
			return new StormPrompt();
		}
	}

	private class StormDurationPrompt extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorEnterDuration");
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			if (input.intValue() < 1) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
				return new StormDurationPrompt();
			}
			context.setSessionData(CK.E_WORLD_STORM_DURATION, input.intValue());
			return new StormPrompt();
		}
	}

	private class ThunderPrompt extends FixedSetPrompt {

		public ThunderPrompt() {
			super("1", "2", "3", "4");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + Lang.get("eventEditorThunderTitle") + "\n";
			if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.GRAY + "2 - " + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + ChatColor.AQUA + ((String) context.getSessionData(CK.E_WORLD_THUNDER)) + ChatColor.YELLOW + ")\n";
				if (context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
				} else {
					int dur = (int) context.getSessionData(CK.E_WORLD_THUNDER_DURATION);
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + ChatColor.AQUA + Quests.getTime(dur * 1000) + ChatColor.YELLOW + ")\n";
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new ThunderWorldPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSetWorldFirst"));
					return new ThunderPrompt();
				} else {
					return new ThunderDurationPrompt();
				}
			} else if (input.equalsIgnoreCase("3")) {
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorThunderCleared"));
				context.setSessionData(CK.E_WORLD_THUNDER, null);
				context.setSessionData(CK.E_WORLD_THUNDER_DURATION, null);
				return new ThunderPrompt();
			} else if (input.equalsIgnoreCase("4")) {
				if (context.getSessionData(CK.E_WORLD_THUNDER) != null && context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetThunderDuration"));
					return new ThunderPrompt();
				} else {
					return new CreateMenuPrompt();
				}
			}
			return null;
		}
	}

	private class ThunderWorldPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String effects = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorWorldsTitle") + "\n" + ChatColor.DARK_PURPLE;
			for (World w : plugin.getServer().getWorlds()) {
				effects += w.getName() + ", ";
			}
			effects = effects.substring(0, effects.length());
			return ChatColor.YELLOW + effects + Lang.get("eventEditorEnterThunderWorld");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (plugin.getServer().getWorld(input) != null) {
					context.setSessionData(CK.E_WORLD_THUNDER, plugin.getServer().getWorld(input).getName());
				} else {
					player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("eventEditorInvalidWorld"));
					return new ThunderWorldPrompt();
				}
			}
			return new ThunderPrompt();
		}
	}

	private class ThunderDurationPrompt extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorEnterDuration");
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			if (input.intValue() < 1) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorAtLeastOneSecond"));
				return new ThunderDurationPrompt();
			} else {
				context.setSessionData(CK.E_WORLD_THUNDER_DURATION, input.intValue());
			}
			return new ThunderPrompt();
		}
	}

	private class MobPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + Lang.get("eventEditorMobSpawnsTitle") + "\n";
			if (context.getSessionData(CK.E_MOB_TYPES) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddMobTypes") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				@SuppressWarnings("unchecked")
				LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
				for (int i = 0; i < types.size(); i++) {
					QuestMob qm = QuestMob.fromString(types.get(i));
					text += ChatColor.BLUE + "" + ChatColor.BOLD + (i + 1) + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("edit") + ": "
							+ ChatColor.AQUA + qm.getType().name() + ((qm.getName() != null) ? ": " + qm.getName() : "") + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + qm.getSpawnAmounts() + ChatColor.GRAY + " -> " + ChatColor.GREEN + Quests.getLocationInfo(qm.getSpawnLocation()) + "\n";
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + (types.size() + 1) + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddMobTypes") + "\n";
				text += ChatColor.RED + "" + ChatColor.BOLD + (types.size() + 2) + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.GREEN + "" + ChatColor.BOLD + (types.size() + 3) + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (context.getSessionData(CK.E_MOB_TYPES) == null) {
				if (input.equalsIgnoreCase("1")) {
					return new QuestMobPrompt(0, null);
				} else if (input.equalsIgnoreCase("2")) {
					context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
					context.setSessionData(CK.E_MOB_TYPES, null);
					return new MobPrompt();
				} else if (input.equalsIgnoreCase("3")) {
					return new CreateMenuPrompt();
				}
			} else {
				@SuppressWarnings("unchecked")
				LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
				int inp;
				try {
					inp = Integer.parseInt(input);
				} catch (NumberFormatException e) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorNotANumber"));
					return new MobPrompt();
				}
				if (inp == types.size() + 1) {
					return new QuestMobPrompt(inp - 1, null);
				} else if (inp == types.size() + 2) {
					context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
					context.setSessionData(CK.E_MOB_TYPES, null);
					return new MobPrompt();
				} else if (inp == types.size() + 3) {
					return new CreateMenuPrompt();
				} else if (inp > types.size()) {
					return new MobPrompt();
				} else {
					return new QuestMobPrompt(inp - 1, QuestMob.fromString(types.get(inp - 1)));
				}
			}
			return new MobPrompt();
		}
	}

	private class QuestMobPrompt extends StringPrompt {

		private QuestMob questMob;
		private Integer itemIndex = -1;
		private final Integer mobIndex;

		public QuestMobPrompt(int mobIndex, QuestMob questMob) {
			this.questMob = questMob;
			this.mobIndex = mobIndex;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + Lang.get("eventEditorAddMobTypesTitle") + "\n";
			if (questMob == null) {
				questMob = new QuestMob();
			}
			// Check/add newly made item
			if (context.getSessionData("newItem") != null) {
				if (itemIndex >= 0) {
					questMob.getInventory()[itemIndex] = ((ItemStack) context.getSessionData("tempStack"));
					itemIndex = -1;
				}
				context.setSessionData("newItem", null);
				context.setSessionData("tempStack", null);
			}
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobName") + ChatColor.GRAY + " (" + ((questMob.getName() == null) ? Lang.get("noneSet") : ChatColor.AQUA + questMob.getName()) + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobType") + ChatColor.GRAY + " (" + ((questMob.getType() == null) ? Lang.get("noneSet") : ChatColor.AQUA + questMob.getType().name()) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorAddSpawnLocation") + ChatColor.GRAY + " (" + ((questMob.getSpawnLocation() == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + Quests.getLocationInfo(questMob.getSpawnLocation())) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobSpawnAmount") + ChatColor.GRAY + " (" + ((questMob.getSpawnAmounts() == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getSpawnAmounts()) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobItemInHand") + ChatColor.GRAY + " (" + ((questMob.getInventory()[0] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob.getInventory()[0])) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobItemInHandDrop") + ChatColor.GRAY + " (" + ((questMob.getDropChances()[0] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[0]) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobBoots") + ChatColor.GRAY + " (" + ((questMob.getInventory()[1] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob.getInventory()[1])) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "8" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobBootsDrop") + ChatColor.GRAY + " (" + ((questMob.getDropChances()[1] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[1]) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "9" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobLeggings") + ChatColor.GRAY + " (" + ((questMob.getInventory()[2] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob.getInventory()[2])) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "10" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobLeggingsDrop") + ChatColor.GRAY + " (" + ((questMob.getDropChances()[2] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[2]) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "11" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobChestPlate") + ChatColor.GRAY + " (" + ((questMob.getInventory()[3] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob.getInventory()[3])) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "12" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobChestPlateDrop") + ChatColor.GRAY + " (" + ((questMob.getDropChances()[3] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[3]) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "13" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobHelmet") + ChatColor.GRAY + " (" + ((questMob.getInventory()[4] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob.getInventory()[4])) + ChatColor.GRAY + ")\n";
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "14" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetMobHelmetDrop") + ChatColor.GRAY + " (" + ((questMob.getDropChances()[4] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[4]) + ChatColor.GRAY + ")\n";
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "15" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done") + "\n";
			text += ChatColor.RED + "" + ChatColor.BOLD + "16" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("cancel");
			return text;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new MobNamePrompt(mobIndex, questMob);
			} else if (input.equalsIgnoreCase("2")) {
				return new MobTypePrompt(mobIndex, questMob);
			} else if (input.equalsIgnoreCase("3")) {
				selectedMobLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
				return new MobLocationPrompt(mobIndex, questMob);
			} else if (input.equalsIgnoreCase("4")) {
				return new MobAmountPrompt(mobIndex, questMob);
			} else if (input.equalsIgnoreCase("5")) {
				itemIndex = 0;
				return new ItemStackPrompt(QuestMobPrompt.this);
			} else if (input.equalsIgnoreCase("6")) {
				return new MobDropPrompt(0, mobIndex, questMob);
			} else if (input.equalsIgnoreCase("7")) {
				itemIndex = 1;
				return new ItemStackPrompt(QuestMobPrompt.this);
			} else if (input.equalsIgnoreCase("8")) {
				return new MobDropPrompt(1, mobIndex, questMob);
			} else if (input.equalsIgnoreCase("9")) {
				itemIndex = 2;
				return new ItemStackPrompt(QuestMobPrompt.this);
			} else if (input.equalsIgnoreCase("10")) {
				return new MobDropPrompt(2, mobIndex, questMob);
			} else if (input.equalsIgnoreCase("11")) {
				itemIndex = 3;
				return new ItemStackPrompt(QuestMobPrompt.this);
			} else if (input.equalsIgnoreCase("12")) {
				return new MobDropPrompt(3, mobIndex, questMob);
			} else if (input.equalsIgnoreCase("13")) {
				itemIndex = 4;
				return new ItemStackPrompt(QuestMobPrompt.this);
			} else if (input.equalsIgnoreCase("14")) {
				return new MobDropPrompt(4, mobIndex, questMob);
			} else if (input.equalsIgnoreCase("15")) {
				if (questMob.getType() == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobTypesFirst"));
					return new QuestMobPrompt(mobIndex, questMob);
				} else if (questMob.getSpawnLocation() == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobLocationFirst"));
					return new QuestMobPrompt(mobIndex, questMob);
				} else if (questMob.getSpawnAmounts() == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobAmountsFirst"));
					return new QuestMobPrompt(mobIndex, questMob);
				}
				if (context.getSessionData(CK.E_MOB_TYPES) == null || ((LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES)).isEmpty()) {
					LinkedList<String> list = new LinkedList<String>();
					list.add(questMob.serialize());
					context.setSessionData(CK.E_MOB_TYPES, list);
				} else {
					LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
					if (mobIndex < list.size()) {
						list.set(mobIndex, questMob.serialize());
					} else {
						list.add(questMob.serialize());
					}
					context.setSessionData(CK.E_MOB_TYPES, list);
				}
				return new MobPrompt();
			} else if (input.equalsIgnoreCase("16")) {
				return new MobPrompt();
			} else {
				return new QuestMobPrompt(mobIndex, questMob);
			}
		}
	}

	private class MobNamePrompt extends StringPrompt {

		private final QuestMob questMob;
		private final Integer mobIndex;

		public MobNamePrompt(int mobIndex, QuestMob questMob) {
			this.questMob = questMob;
			this.mobIndex = mobIndex;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.YELLOW + Lang.get("eventEditorSetMobNamePrompt");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new QuestMobPrompt(mobIndex, questMob);
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				questMob.setName(null);
				return new QuestMobPrompt(mobIndex, questMob);
			} else {
				input = ChatColor.translateAlternateColorCodes('&', input);
				questMob.setName(input);
				return new QuestMobPrompt(mobIndex, questMob);
			}
		}
	}

	private class MobTypePrompt extends StringPrompt {

		private final QuestMob questMob;
		private final Integer mobIndex;

		public MobTypePrompt(int mobIndex, QuestMob questMob) {
			this.questMob = questMob;
			this.mobIndex = mobIndex;
		}

		@Override
		public String getPromptText(ConversationContext arg0) {
			String mobs = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorMobsTitle") + "\n";
			final EntityType[] mobArr = EntityType.values();
			for (int i = 0; i < mobArr.length; i++) {
				final EntityType type = mobArr[i];
				if (type.isAlive() == false) {
					continue;
				}
				if (i < (mobArr.length - 1)) {
					mobs += MiscUtil.getProperMobName(mobArr[i]) + ", ";
				} else {
					mobs += MiscUtil.getProperMobName(mobArr[i]) + "\n";
				}
			}
			return mobs + ChatColor.YELLOW + Lang.get("eventEditorSetMobTypesPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (MiscUtil.getProperMobType(input) != null) {
					questMob.setType(MiscUtil.getProperMobType(input));
				} else {
					player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("eventEditorInvalidMob"));
					return new MobTypePrompt(mobIndex, questMob);
				}
			}
			return new QuestMobPrompt(mobIndex, questMob);
		}
	}

	private class MobAmountPrompt extends StringPrompt {

		private final QuestMob questMob;
		private final Integer mobIndex;

		public MobAmountPrompt(int mobIndex, QuestMob questMob) {
			this.questMob = questMob;
			this.mobIndex = mobIndex;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetMobAmountsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int i = Integer.parseInt(input);
					if (i < 1) {
						player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
						return new MobAmountPrompt(mobIndex, questMob);
					}
					questMob.setSpawnAmounts(i);
					return new QuestMobPrompt(mobIndex, questMob);
				} catch (NumberFormatException e) {
					player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("eventEditorNotANumber"));
					return new MobAmountPrompt(mobIndex, questMob);
				}
			}
			return new QuestMobPrompt(mobIndex, questMob);
		}
	}

	private class MobLocationPrompt extends StringPrompt {

		private final QuestMob questMob;
		private final Integer mobIndex;

		public MobLocationPrompt(int mobIndex, QuestMob questMob) {
			this.questMob = questMob;
			this.mobIndex = mobIndex;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetMobLocationPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
				Block block = selectedMobLocations.get(player.getUniqueId());
				if (block != null) {
					Location loc = block.getLocation();
					questMob.setSpawnLocation(loc);
					selectedMobLocations.remove(player.getUniqueId());
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
					return new MobLocationPrompt(mobIndex, questMob);
				}
				return new QuestMobPrompt(mobIndex, questMob);
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				selectedMobLocations.remove(player.getUniqueId());
				return new QuestMobPrompt(mobIndex, questMob);
			} else {
				return new MobLocationPrompt(mobIndex, questMob);
			}
		}
	}

	private class MobDropPrompt extends StringPrompt {

		private final QuestMob questMob;
		private final Integer mobIndex;
		private final Integer invIndex;

		public MobDropPrompt(int invIndex, int mobIndex, QuestMob questMob) {
			this.questMob = questMob;
			this.mobIndex = mobIndex;
			this.invIndex = invIndex;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.YELLOW + Lang.get("eventEditorSetDropChance");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			float chance;
			if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new QuestMobPrompt(mobIndex, questMob);
			}
			try {
				chance = Float.parseFloat(input);
			} catch (NumberFormatException e) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
						.replace("<least>", "0.0").replace("<greatest>", "1.0"));
				return new MobDropPrompt(invIndex, mobIndex, questMob);
			}
			if (chance > 1 || chance < 0) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
						.replace("<least>", "0.0").replace("<greatest>", "1.0"));
				return new MobDropPrompt(invIndex, mobIndex, questMob);
			}
			Float[] temp = questMob.getDropChances();
			temp[invIndex] = chance;
			questMob.setDropChances(temp);
			return new QuestMobPrompt(mobIndex, questMob);
		}
	}

	private class LightningPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorLightningPrompt");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
				Block block = selectedLightningLocations.get(player.getUniqueId());
				if (block != null) {
					Location loc = block.getLocation();
					LinkedList<String> locs;
					if (context.getSessionData(CK.E_LIGHTNING) != null) {
						locs = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);
					} else {
						locs = new LinkedList<String>();
					}
					locs.add(Quests.getLocationInfo(loc));
					context.setSessionData(CK.E_LIGHTNING, locs);
					selectedLightningLocations.remove(player.getUniqueId());
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
					return new LightningPrompt();
				}
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.E_LIGHTNING, null);
				selectedLightningLocations.remove(player.getUniqueId());
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				selectedLightningLocations.remove(player.getUniqueId());
				return new CreateMenuPrompt();
			} else {
				return new LightningPrompt();
			}
		}
	}

	private class PotionEffectPrompt extends FixedSetPrompt {

		public PotionEffectPrompt() {
			super("1", "2", "3", "4", "5");
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + Lang.get("eventEditorPotionEffectsTitle") + "\n";
			if (context.getSessionData(CK.E_POTION_TYPES) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetPotionEffectTypes") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.GRAY + "2 - " + Lang.get("eventEditorSetPotionDurations") + " " + Lang.get("noneSet") + "\n";
				text += ChatColor.GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " " + Lang.get("noneSet") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetPotionEffectTypes") + "\n";
				for (String s : (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES)) {
					text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
				}
				if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetPotionDurations") + " (" + Lang.get("noneSet") + ")\n";
					text += ChatColor.GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " " + Lang.get("noneSet") + "\n";
				} else {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("noneSet") + "\n";
					for (Long l : (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS)) {
						text += ChatColor.GRAY + "    - " + ChatColor.DARK_AQUA + Quests.getTime(l * 50L) + "\n";
					}
					if (context.getSessionData(CK.E_POTION_STRENGHT) == null) {
						text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetPotionMagnitudes") + " (" + Lang.get("noneSet") + ")\n";
					} else {
						text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("eventEditorSetPotionMagnitudes") + "\n";
						for (int i : (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGHT)) {
							text += ChatColor.GRAY + "    - " + ChatColor.DARK_PURPLE + i + "\n";
						}
					}
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new PotionTypesPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				if (context.getSessionData(CK.E_POTION_TYPES) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetPotionTypesFirst"));
					return new PotionEffectPrompt();
				} else {
					return new PotionDurationsPrompt();
				}
			} else if (input.equalsIgnoreCase("3")) {
				if (context.getSessionData(CK.E_POTION_TYPES) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetPotionTypesAndDurationsFirst"));
					return new PotionEffectPrompt();
				} else if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetPotionDurationsFirst"));
					return new PotionEffectPrompt();
				} else {
					return new PotionMagnitudesPrompt();
				}
			} else if (input.equalsIgnoreCase("4")) {
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorPotionsCleared"));
				context.setSessionData(CK.E_POTION_TYPES, null);
				context.setSessionData(CK.E_POTION_DURATIONS, null);
				context.setSessionData(CK.E_POTION_STRENGHT, null);
				return new PotionEffectPrompt();
			} else if (input.equalsIgnoreCase("5")) {
				int one;
				int two;
				int three;
				if (context.getSessionData(CK.E_POTION_TYPES) != null) {
					one = ((List<String>) context.getSessionData(CK.E_POTION_TYPES)).size();
				} else {
					one = 0;
				}
				if (context.getSessionData(CK.E_POTION_DURATIONS) != null) {
					two = ((List<Long>) context.getSessionData(CK.E_POTION_DURATIONS)).size();
				} else {
					two = 0;
				}
				if (context.getSessionData(CK.E_POTION_STRENGHT) != null) {
					three = ((List<Integer>) context.getSessionData(CK.E_POTION_STRENGHT)).size();
				} else {
					three = 0;
				}
				if (one == two && two == three) {
					return new CreateMenuPrompt();
				} else {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorListSizeMismatch"));
					return new PotionEffectPrompt();
				}
			}
			return null;
		}
	}

	private class PotionTypesPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String effs = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorPotionTypesTitle") + "\n";
			for (PotionEffectType pet : PotionEffectType.values()) {
				effs += (pet != null && pet.getName() != null) ? (ChatColor.DARK_PURPLE + pet.getName() + "\n") : "";
			}
			return effs + ChatColor.YELLOW + Lang.get("eventEditorSetPotionEffectsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				LinkedList<String> effTypes = new LinkedList<String>();
				for (String s : input.split(" ")) {
					if (PotionEffectType.getByName(s.toUpperCase()) != null) {
						effTypes.add(PotionEffectType.getByName(s.toUpperCase()).getName());
						context.setSessionData(CK.E_POTION_TYPES, effTypes);
					} else {
						player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED + Lang.get("eventEditorInvalidPotionType"));
						return new PotionTypesPrompt();
					}
				}
			}
			return new PotionEffectPrompt();
		}
	}

	private class PotionDurationsPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetPotionDurationsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				LinkedList<Long> effDurations = new LinkedList<Long>();
				for (String s : input.split(" ")) {
					try {
						int i = Integer.parseInt(s);
						long l = i * 1000;
						if (l < 1000) {
							player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
							return new PotionDurationsPrompt();
						}
						effDurations.add(l / 50L);
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED + Lang.get("eventEditorNotANumber"));
						return new PotionDurationsPrompt();
					}
				}
				context.setSessionData(CK.E_POTION_DURATIONS, effDurations);
			}
			return new PotionEffectPrompt();
		}
	}

	private class PotionMagnitudesPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetPotionMagnitudesPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				LinkedList<Integer> magAmounts = new LinkedList<Integer>();
				for (String s : input.split(" ")) {
					try {
						int i = Integer.parseInt(s);
						if (i < 1) {
							player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
							return new PotionMagnitudesPrompt();
						}
						magAmounts.add(i);
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED + Lang.get("eventEditorNotANumber"));
						return new PotionMagnitudesPrompt();
					}
				}
				context.setSessionData(CK.E_POTION_STRENGHT, magAmounts);
			}
			return new PotionEffectPrompt();
		}
	}

	private class HungerPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetHungerPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				try {
					int i = Integer.parseInt(input);
					if (i < 0) {
						((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "0"));
						return new HungerPrompt();
					} else {
						context.setSessionData(CK.E_HUNGER, (Integer) i);
					}
				} catch (NumberFormatException e) {
					context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
							+ Lang.get("stageEditorInvalidNumber"));
					return new HungerPrompt();
				}
			} else {
				context.setSessionData(CK.E_HUNGER, null);
			}
			return new CreateMenuPrompt();
		}
	}

	private class SaturationPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetSaturationPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				try {
					int i = Integer.parseInt(input);
					if (i < 0) {
						((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "0"));
						return new SaturationPrompt();
					} else {
						context.setSessionData(CK.E_SATURATION, (Integer) i);
					}
				} catch (NumberFormatException e) {
					context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
							+ Lang.get("stageEditorInvalidNumber"));
					return new SaturationPrompt();
				}
			} else {
				context.setSessionData(CK.E_SATURATION, null);
			}
			return new CreateMenuPrompt();
		}
	}

	private class HealthPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetHealthPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				try {
					int i = Integer.parseInt(input);
					if (i < 0) {
						((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "0"));
						return new HealthPrompt();
					} else {
						context.setSessionData(CK.E_HEALTH, (Integer) i);
					}
				} catch (NumberFormatException e) {
					context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
							+ Lang.get("stageEditorInvalidNumber"));
					return new HealthPrompt();
				}
			} else {
				context.setSessionData(CK.E_HEALTH, null);
			}
			return new CreateMenuPrompt();
		}
	}

	private class TeleportPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("eventEditorSetTeleportPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdDone"))) {
				Block block = selectedTeleportLocations.get(player.getUniqueId());
				if (block != null) {
					Location loc = block.getLocation();
					context.setSessionData(CK.E_TELEPORT, Quests.getLocationInfo(loc));
					selectedTeleportLocations.remove(player.getUniqueId());
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
					return new TeleportPrompt();
				}
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.E_TELEPORT, null);
				selectedTeleportLocations.remove(player.getUniqueId());
				return new CreateMenuPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				selectedTeleportLocations.remove(player.getUniqueId());
				return new CreateMenuPrompt();
			} else {
				return new TeleportPrompt();
			}
		}
	}

	private class CommandsPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "" + ChatColor.ITALIC + Lang.get("eventEditorCommandsNote");
			return ChatColor.YELLOW + Lang.get("eventEditorSetCommandsPrompt") + "\n" + text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				String[] commands = input.split(Lang.get("charSemi"));
				LinkedList<String> cmdList = new LinkedList<String>();
				cmdList.addAll(Arrays.asList(commands));
				context.setSessionData(CK.E_COMMANDS, cmdList);
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.E_COMMANDS, null);
			}
			return new CreateMenuPrompt();
		}
	}
}