package spigot.madfout.mdftrimenchants.model;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

public class MdfTrimEffect {

    private final Attribute attribute;
    private final double value;
    private final AttributeModifier.Operation operation;

    public MdfTrimEffect(Attribute attribute, double value, AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.value = value;
        this.operation = operation;
    }

    public Attribute getAttribute()                 { return attribute; }
    public double getValue()                        { return value; }
    public AttributeModifier.Operation getOperation() { return operation; }

}