package net.darkhax.upgrademodifiers;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;

import net.darkhax.bookshelf.registry.RegistryHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("upgrademodifiers")
public class UpgradeModifiers {
    
    private static final EquipmentSlotType[] ARMOR_SLOTS = new EquipmentSlotType[] { EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET };
    
    private final Logger logger = LogManager.getLogger("Upgrade Modifiers");
    private final RegistryHelper registry = new RegistryHelper("upgrademodifiers", this.logger, ItemGroup.MISC);
    private final Map<Rarity, Integer> rarityCost = new HashMap<>();
    
    private final Item modifierArmorToughness;
    private final Item modifierDamage;
    private final Item modifierKnockback;
    private final Item modifierLuck;
    private final Item modifierHealth;
    private final Item modifierSpeed;
    private final Item modifierReach;
    private final Item modifierArmor;
    
    public UpgradeModifiers() {
        
        this.rarityCost.put(Rarity.COMMON, 5);
        this.rarityCost.put(Rarity.UNCOMMON, 10);
        this.rarityCost.put(Rarity.RARE, 15);
        this.rarityCost.put(Rarity.EPIC, 20);
        
        MinecraftForge.EVENT_BUS.addListener(this::onAnvilUpdateEvent);
        
        this.modifierArmorToughness = this.registry.registerItem(new ItemModifier(Rarity.UNCOMMON, SharedMonsterAttributes.ARMOR_TOUGHNESS, 1d, Operation.ADDITION, ARMOR_SLOTS), "modifier_armor_toughness");
        this.modifierDamage = this.registry.registerItem(new ItemModifier(Rarity.RARE, SharedMonsterAttributes.ATTACK_DAMAGE, 1.5d, Operation.ADDITION, EquipmentSlotType.MAINHAND), "modifier_attack_damage");
        this.modifierKnockback = this.registry.registerItem(new ItemModifier(Rarity.COMMON, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, 0.1, Operation.MULTIPLY_BASE, EquipmentSlotType.CHEST), "modifier_knockback_resistance");
        this.modifierLuck = this.registry.registerItem(new ItemModifier(Rarity.EPIC, SharedMonsterAttributes.LUCK, 1, Operation.ADDITION, EquipmentSlotType.MAINHAND), "modifier_luck");
        this.modifierHealth = this.registry.registerItem(new ItemModifier(Rarity.EPIC, SharedMonsterAttributes.MAX_HEALTH, 1f, Operation.ADDITION, EquipmentSlotType.CHEST), "modifier_max_health");
        this.modifierSpeed = this.registry.registerItem(new ItemModifier(Rarity.COMMON, SharedMonsterAttributes.MOVEMENT_SPEED, 0.1, Operation.MULTIPLY_BASE, EquipmentSlotType.FEET), "modifier_movement_speed");
        this.modifierReach = this.registry.registerItem(new ItemModifier(Rarity.UNCOMMON, PlayerEntity.REACH_DISTANCE, 1.5, Operation.ADDITION, EquipmentSlotType.MAINHAND), "modifier_block_reach");
        this.modifierArmor = this.registry.registerItem(new ItemModifier(Rarity.RARE, SharedMonsterAttributes.ARMOR, 1, Operation.ADDITION, ARMOR_SLOTS), "modifier_armor");
        
        for (ItemModifier itemModifier : ItemModifier.modifierItems) {
            
            ItemStack itemStack = new ItemStack(itemModifier);
            this.registry.addRareWanderingTrade(new BasicTrade(15 * (itemModifier.getRarity(itemStack).ordinal() + 1), itemStack, 1, 5));
        }
        
        this.registry.initialize(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private void onAnvilUpdateEvent (AnvilUpdateEvent event) {
        
        final Item rightItem = event.getRight().getItem();
        
        if (rightItem instanceof ItemModifier) {
            
            final ItemModifier modifierItem = (ItemModifier) rightItem;
            final EquipmentSlotType inputSlotType = MobEntity.getSlotForItemStack(event.getLeft());
            
            for (final EquipmentSlotType slotType : modifierItem.getSlots()) {
                
                if (slotType == inputSlotType) {
                    
                    final ItemStack output = event.getLeft().copy();
                    
                    if (applyUpgrades(output, modifierItem, slotType)) {
                        
                        event.setCost(this.rarityCost.get(modifierItem.getRarity(event.getRight())));
                        event.setMaterialCost(1);
                        copyDefaultAttributes(output, slotType);
                        event.setOutput(output);
                    }
                    
                    break;
                }
            }
        }
    }
    
    private static boolean applyUpgrades (ItemStack item, ItemModifier modifierItem, EquipmentSlotType slot) {
        
        final CompoundNBT tag = item.getOrCreateTag();
        
        // Get the list of UUIDs for all upgrade modifiers.
        final ListNBT upgradeTags = tag.getList("UpgradeModifierIds", NBT.TAG_STRING);
        
        // If there are less than three existing modifiers, add a new one.
        if (upgradeTags.size() < 3) {
            
            final AttributeModifier modifier = modifierItem.getModifier();
            item.addAttributeModifier(modifierItem.getAttribute().getName(), modifier, slot);
            
            // Update the list to include the new modifier.
            upgradeTags.add(new StringNBT(modifier.getID().toString()));
            tag.put("UpgradeModifierIds", upgradeTags);
            return true;
        }
        
        return false;
    }
    
    private static void copyDefaultAttributes (ItemStack item, EquipmentSlotType slot) {
        
        final Multimap<String, AttributeModifier> originalModifiers = item.getItem().getAttributeModifiers(slot, item);
        
        for (final String key : originalModifiers.keySet()) {
            
            for (final AttributeModifier modifier : originalModifiers.get(key)) {
                
                item.addAttributeModifier(key, modifier, slot);
            }
        }
    }
}