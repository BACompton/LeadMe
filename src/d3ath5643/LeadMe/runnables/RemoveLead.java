package d3ath5643.LeadMe.runnables;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import d3ath5643.LeadMe.Main;

public class RemoveLead extends BukkitRunnable{
    private Entity e;
    private Main plugin;
    
    public RemoveLead(Main plugin, Entity e)
    {
        this.plugin = plugin;
        this.e = e;
    }
    
    @Override
    public void run() {
        for(Entity ent: e.getNearbyEntities(plugin.getXRadius(e.getType()), 
                plugin.getYRadius(e.getType()), 
                plugin.getZRadius(e.getType())))
            if(ent.getType() == EntityType.DROPPED_ITEM && (ent.getCustomName() == null || !ent.getCustomName().equals("Remove"))
                    && ((Item)ent).getItemStack().getType() == Material.LEASH)
            {
                Item item = (Item) ent;
                
                if(item.getItemStack().getAmount() > 1)
                {
                    item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                }
                else
                {
                    item.setCustomName("Remove");
                    item.remove();
                }
                break;
            }
        this.cancel();
    }

}
