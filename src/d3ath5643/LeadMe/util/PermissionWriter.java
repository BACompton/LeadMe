package d3ath5643.LeadMe.util;

import org.bukkit.entity.EntityType;

public class PermissionWriter {
    public static void main(String[] args)
    {
        for(EntityType et: EntityType.values())
            if(et.isAlive())
                System.out.println("      LeadMe.leash." + et.toString().toLowerCase() + ": true");
        for(EntityType et: EntityType.values())
            if(et.isAlive())
            {
                System.out.println("  LeadMe.leash." + et.toString().toLowerCase() + ":");
                System.out.println("    description: Gives ability to leash a " + et.toString());
                System.out.println("    default: op");
            }
    }
}
