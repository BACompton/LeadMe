package d3ath5643.LeadMe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import d3ath5643.LeadMe.commands.Reload;
import d3ath5643.LeadMe.listener.LeadListener;
import d3ath5643.LeadMe.listener.PersistListener;

public class Main extends JavaPlugin{
    private HashMap<UUID, List<UUID>> offlineLeads;
    //TODO: Check why always cancel event
    @Override
    public void onEnable()
    {
        createNewConfig();
        registerCommands();
        loadOfflineLeads();
        new LeadListener(this);
        new PersistListener(this);
    }
    
    @Override
    public void onDisable()
    {
        for(World w: getServer().getWorlds())
            for(Player p: w.getPlayers())
            {
                List<UUID> entUUID = new ArrayList<UUID>();
                
                for(LivingEntity le: w.getLivingEntities())
                    if(le.isLeashed() && le.getLeashHolder().equals(p) && persist(le.getType()))
                        entUUID.add(le.getUniqueId());
                put(p.getUniqueId(), entUUID);
            }
        
        Iterator<Entry<UUID, List<UUID>>> iter = offlineLeads.entrySet().iterator();
        YamlConfiguration offlineYml = new YamlConfiguration();
        
        while(iter.hasNext())
        {
            Entry<UUID, List<UUID>> next = iter.next();
            List<String> entStr = new ArrayList<String>();
            
            for(UUID uuid: next.getValue())
                entStr.add(uuid.toString());
            
            offlineYml.set(next.getKey().toString(), entStr);
        }
        
        try {
            offlineYml.save(new File("plugins/LeadMe/offlineLeads.yml"));
        } catch (FileNotFoundException e) {
            getLogger().warning("offlineYaml.yml could not be found! Check to see if it was created when LeadMe was enabled.");
        } catch (IOException e) {
            getLogger().warning("Unable to access offlineYaml.yml! Could not save any of the offline leads.");
        }
    }

    public void createNewConfig() {
        File config = new File("plugins/LeadMe/config.yml");
        if(!config.exists())
        {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        
        File offlineYaml = new File("plugins/LeadMe/offlineLeads.yml");
        if(!offlineYaml.exists())
        {
            try {
                offlineYaml.createNewFile();
                getLogger().info("Successfully created offlineYaml.yml!");
            } catch (IOException e) {
                getLogger().warning("Unable to create offlineYaml.yml! Leads will cannot persist after logout");
            }
        }
    }

    public boolean canLeash(EntityType type)
    {
        if(type == EntityType.PLAYER)
            return false;
        if(getConfig().isBoolean(type.toString() + ".leash"))
            return getConfig().getBoolean(type.toString() + ".leash");
        return getConfig().getBoolean("default.leash");
    }
    
    public boolean overrideDefault(EntityType type)
    {
        if(getConfig().isBoolean(type.toString() + ".overrideDefault"))
            return getConfig().getBoolean(type.toString() + ".overrideDefault");
        return getConfig().getBoolean("default.overrideDefault");
    }
    
    public Presidence getPresidence(EntityType type)
    {
        if(getConfig().isString(type.toString() + ".presidence"))
            return Presidence.valueOf(getConfig().getString(type.toString() + ".presidence"));
        return Presidence.valueOf(getConfig().getString("default.presidence"));
    }
    
    public boolean persist(EntityType type)
    {
        if(getConfig().isBoolean(type.toString() + ".persistOnLoggout"))
            return getConfig().getBoolean(type.toString() + ".persistOnLoggout");
        return getConfig().getBoolean("default.persistOnLoggout");
    }
    
    public double getXRadius(EntityType type)
    {
        if(getConfig().isDouble(type.toString() + ".xRadius"))
            return getConfig().getDouble(type.toString() + ".xRadius");
        return getConfig().getDouble("default.xRadius");
    }
    
    public double getYRadius(EntityType type)
    {
        if(getConfig().isDouble(type.toString() + ".yRadius"))
            return getConfig().getDouble(type.toString() + ".yRadius");
        return getConfig().getDouble("default.yRadius");
    }
    
    public double getZRadius(EntityType type)
    {
        if(getConfig().isDouble(type.toString() + ".zRadius"))
            return getConfig().getDouble(type.toString() + ".zRadius");
        return getConfig().getDouble("default.zRadius");
    }
    
    public void put(UUID key, List<UUID> value)
    {
        offlineLeads.put(key, value);
    }
    
    public void remove(UUID key)
    {
        offlineLeads.remove(key);
    }
    
    public List<UUID> get(UUID key)
    {
        return offlineLeads.get(key);
    }
    
    public boolean containsKey(UUID key)
    {
        return offlineLeads.containsKey(key);
    }
    
    public boolean containsPossibleLeash(UUID uuid)
    {
        for(UUID key: offlineLeads.keySet())
            if(offlineLeads.get(key).contains(uuid))
                return true;
        return false;
    }

    public void removePossibleLeash(UUID uuid) {
        for(UUID key: offlineLeads.keySet())
            if(offlineLeads.get(key).contains(uuid))
                offlineLeads.remove(uuid);
    }
    
    private void loadOfflineLeads() {
        offlineLeads = new HashMap<UUID, List<UUID>>();
        YamlConfiguration offlineYml = new YamlConfiguration();
        
        try {
            offlineYml.load(new File("plugins/LeadMe/offlineLeads.yml"));
            for(String key:offlineYml.getKeys(false))
            {
                List<UUID> entsUUID = new ArrayList<UUID>();
                for(String str: offlineYml.getStringList(key))
                    entsUUID.add(UUID.fromString(str));
                
                offlineLeads.put(UUID.fromString(key), entsUUID);
            }
        } catch (FileNotFoundException e) {
            getLogger().warning("offlineYaml.yml could not be found! Check to see if it was created when LeadMe was enabled.");
        } catch (IOException e) {
            getLogger().warning("Unable to access offlineYaml.yml! Could not load any of the offline leads.");
        } catch (InvalidConfigurationException e) {
            getLogger().warning("Unable to load any of the offline leads! Did you edit this file?");
        }
    }

    private void registerCommands() {
        //TODO: add command leashable
        getCommand("lmreload").setExecutor(new Reload(this));
        
    }
}
