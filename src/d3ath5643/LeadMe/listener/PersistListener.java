package d3ath5643.LeadMe.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import d3ath5643.LeadMe.Main;
import d3ath5643.LeadMe.runnables.RemoveLead;

public class PersistListener implements Listener{
    private Main plugin;
    
    public PersistListener(Main plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e)
    {
        List<UUID> entUUID = new ArrayList<UUID>();
        for(LivingEntity le : e.getPlayer().getWorld().getLivingEntities())
            if(le.isLeashed() && le.getLeashHolder().equals(e.getPlayer())
                    && plugin.persist(le.getType()))
                entUUID.add(le.getUniqueId());
        plugin.put(e.getPlayer().getUniqueId(), entUUID);
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e)
    {
        if(plugin.persist(e.getEntityType()))
            plugin.removePossibleLeash(e.getEntity().getUniqueId());
    }
    
    @EventHandler
    public void onEntityUnleash(EntityUnleashEvent e)
    {
        if(plugin.persist(e.getEntityType())
                && e.getReason() == EntityUnleashEvent.UnleashReason.HOLDER_GONE 
                && plugin.containsPossibleLeash(e.getEntity().getUniqueId()))
            new RemoveLead(plugin, e.getEntity()).runTaskLater(plugin, 1);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if(plugin.containsKey(e.getPlayer().getUniqueId()))
        {
            List<UUID> entUUID = plugin.get(e.getPlayer().getUniqueId());
            
            for(LivingEntity le: e.getPlayer().getWorld().getLivingEntities())
                if(entUUID.contains(le.getUniqueId()))
                {
                    if(le.isLeashed() || le.getLocation().distance(e.getPlayer().getLocation())-10.0 > 0)
                        e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), new ItemStack(Material.LEASH));
                    else
                    	le.setLeashHolder(e.getPlayer());
                }
            plugin.remove(e.getPlayer().getUniqueId());
        }
    }
}
