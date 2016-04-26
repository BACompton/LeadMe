package d3ath5643.LeadMe.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import d3ath5643.LeadMe.Main;

public class LeadListener implements Listener{
    private Main plugin;
    
    public LeadListener(Main plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        if(e.getRightClicked().getType().isAlive() && plugin.canLeash(e.getRightClicked().getType())
                && e.getPlayer().hasPermission("LeadMe.leash." + e.getRightClicked().getType().toString().toLowerCase()))
        {
            LivingEntity le = (LivingEntity) e.getRightClicked();
            
            if(!le.isLeashed() & !e.getPlayer().isSneaking() 
                    && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.LEASH)
            {
                le.setLeashHolder(e.getPlayer());
                removeLead(e.getPlayer(), 1);
                e.setCancelled(true);
            }
            else if(le.isLeashed() && le.getLeashHolder().equals(e.getPlayer()))
            {
                le.setLeashHolder(null);
                dropLead(e.getPlayer(), le.getLocation(), 1);
                e.setCancelled(true);
            }
            else
            {   //TODO: Create method for players to toggle state
                List<LivingEntity> lents = e.getPlayer().getWorld().getLivingEntities();
                boolean switched; 
                switch(plugin.getPresidence(e.getRightClicked().getType()))
                {
                    case PLAYER:
                        switched = swapLeadToPlayer(lents, e);
                        if(!switched)
                            swapLeadToEntity(lents, e);
                        break;
                    case ENTITY:
                        switched = swapLeadToEntity(lents, e);
                        if(!switched)
                            swapLeadToPlayer(lents, e);
                        break;
                    case NONE:
                    default:
                        swapLeadToBoth(lents, e);
                        break;
                }
                e.setCancelled(true);
            }
        }
        else if(plugin.overrideDefault(e.getRightClicked().getType()))
        {
            e.setCancelled(true);
            e.getPlayer().updateInventory();
        }
    }
    
    private void removeLead(Player p, int amount)
    {
        if(p.getGameMode() != GameMode.CREATIVE)
        {
            if(p.getItemInHand().getType() == Material.LEASH)
            {
            	if(p.getItemInHand().getAmount() > amount)
            	    p.getItemInHand().setAmount(p.getItemInHand().getAmount() - amount);
            	else
            	{
            	    int extra = amount - p.getItemInHand().getAmount();
            	    p.setItemInHand(null);
            	    if(extra > 0)
            	        removeLead(p, extra);
            	}
            }
            else
            {
                int left = amount;
                for(ItemStack item: p.getInventory().getContents())
                    if(item.getType() == Material.LEASH)
                    {
                        if(item.getAmount() > left)
                            item.setAmount(item.getAmount() - left);
                        else
                        {
                            left -= item.getAmount();
                            p.getInventory().remove(item);
                        }
                    }
            }
        }
    }
    
    private void dropLead(Player p, Location loc, int amount)
    {
        if(p.getGameMode() != GameMode.CREATIVE)
            p.getWorld().dropItemNaturally(loc, new ItemStack(Material.LEASH, amount));
    }
    
    private boolean swapLeadToPlayer(List<LivingEntity> lents, PlayerInteractEntityEvent e)
    {
        boolean switched = false;
        
        for(LivingEntity lent: lents)
            if(swapPlayer(lent, e, true))
                switched = true;
        return switched;
    }
    
    private boolean swapLeadToEntity(List<LivingEntity> lents, PlayerInteractEntityEvent e)
    {
        boolean switched = false;
        
        for(LivingEntity lent: lents)
            if(swapEntity(lent, e, 0))
                switched = true;
        return switched;
    }
    
    private void swapLeadToBoth(List<LivingEntity> lents, PlayerInteractEntityEvent e)
    {
        int leadsDropped = 0;
        List<LivingEntity> swapEnt = new ArrayList<LivingEntity>(),
                           swapPlayer = new ArrayList<LivingEntity>();
        
        for(LivingEntity lent: lents)
        {
            if(swapPlayer(lent, e, false))
            {
                swapPlayer.add(lent);
                leadsDropped++;
            }
            else
                swapEnt.add(lent);
        }
        for(LivingEntity lent: swapEnt)
            if(swapEntity(lent, e, leadsDropped))
                leadsDropped--;
        
        if(leadsDropped > 0)
            dropLead(e.getPlayer(), e.getRightClicked().getLocation(), leadsDropped);
    }
    
    private boolean swapEntity(LivingEntity lent, PlayerInteractEntityEvent e, int leadsDropped) {
        LivingEntity le = (LivingEntity)e.getRightClicked();
        
        if((leadsDropped > 0 || e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.LEASH)
                && !lent.equals(le) && lent.isLeashed() && lent.getLeashHolder().equals(e.getPlayer())
                && (e.getPlayer().isSneaking() ^ le.isLeashed()))
        {
            if(leadsDropped <= 0)
                removeLead(e.getPlayer(), 1);
            
            lent.setLeashHolder(e.getRightClicked());
            e.setCancelled(true);
            return true;
        }
        return false;
    }

    private boolean swapPlayer(LivingEntity lent, PlayerInteractEntityEvent e, boolean drop)
    {
        if(e.getPlayer().isSneaking() && lent.isLeashed() && lent.getLeashHolder().equals(e.getRightClicked()))
        {
            if(drop)
                dropLead(e.getPlayer(), e.getRightClicked().getLocation(), 1);
            
            lent.setLeashHolder(e.getPlayer());
            e.setCancelled(true);
            return true;
        }
        return false;
    }
}
