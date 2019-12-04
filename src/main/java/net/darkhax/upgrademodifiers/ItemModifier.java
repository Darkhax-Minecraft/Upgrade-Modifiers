package net.darkhax.upgrademodifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemModifier extends Item {
    
    public static final List<ItemModifier> modifierItems = new ArrayList<>(); 
    
    /**
     * The attribute affected by this modifier.
     */
    private final IAttribute attribute;
    
    /**
     * The amount to modify per level of the modifier.
     */
    private final double amount;
    
    /**
     * The modifier operation to use.
     */
    private final Operation operation;
    
    /**
     * An array of slots the modifier can be applied to.
     */
    private final EquipmentSlotType[] slots;
    
    public ItemModifier(Rarity rarity, IAttribute attribute, double amount, Operation operation, EquipmentSlotType... slots) {
        
        super(new Properties().group(ItemGroup.MISC).maxStackSize(1).rarity(rarity));
        
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
        this.slots = slots;
        
        modifierItems.add(this);
    }
    
    public IAttribute getAttribute () {
        
        return this.attribute;
    }
    
    public double getAmount () {
        
        return this.amount;
    }
    
    public Operation getOperation () {
        
        return this.operation;
    }
    
    public EquipmentSlotType[] getSlots () {
        
        return this.slots;
    }
    
    public AttributeModifier getModifier () {
        
        return new AttributeModifier(UUID.randomUUID(), "Upgrade Modifier " + this.getAttribute().getName(), this.getAmount(), this.operation);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation (ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        
        String descString = "item." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".desc";
        tooltip.add(new TranslationTextComponent("item." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".desc"));
    }
}