package d3ath5643.LeadMe;

public enum Presidence 
{
    NONE("NONE"), PLAYER("PLAYER"), ENTITY("ENTITY");
    
    private String value;
    
    private Presidence(String val)
    {
        value = val;
    }
    
    @Override
    public String toString()
    {
        return value;
    }
}
