package net.darkhax.bookshelf.api.util;

import net.darkhax.bookshelf.mixin.entity.AccessorEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class TextHelper {

    public static MutableComponent getFormatedTime(int ticks, boolean includeHover) {

        MutableComponent component = new TextComponent(StringUtil.formatTickDuration(ticks));

        if (includeHover) {

            component = setHover(component, new TranslatableComponent("text.bookshelf.ticks", ticks));
        }

        return component;
    }

    public static <T extends Component> T setHover(T component, Component hoverInfo) {

        return setHover(component, HoverEvent.Action.SHOW_TEXT, hoverInfo);
    }

    public static <T extends Component> T setHover(T component, ItemStack hoverInfo) {

        return setHover(component, HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(hoverInfo));
    }

    public static <T extends Component> T setHover(T component, Entity hoverInfo) {

        if (hoverInfo instanceof AccessorEntity accessor) {

            return setHover(component, accessor.bookshelf$createHoverEvent());
        }

        return setHover(component, HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(hoverInfo.getType(), hoverInfo.getUUID(), hoverInfo.getName()));
    }

    public static <T extends Component, AT, A extends HoverEvent.Action<AT>> T setHover(T component, A action, AT arg) {

        return setHover(component, new HoverEvent(action, arg));
    }

    public static <T extends Component, AT, A extends HoverEvent.Action<AT>> T setHover(T component, HoverEvent hoverInfo) {

        if (component instanceof MutableComponent mutable) {

            mutable.withStyle(style -> style.withHoverEvent(hoverInfo));
        }

        return component;
    }

    /**
     * Recursively applies a custom font to a text component and all of it's children components.
     *
     * @param text The text to apply the font to.
     * @param font The ID of the font to apply.
     * @return A modified text component that has had the font applied.
     */
    public static Component applyFont(Component text, ResourceLocation font) {

        if (text instanceof MutableComponent mutable) {

            mutable.withStyle(style -> style.withFont(font));
        }

        text.getSiblings().forEach(sib -> applyFont(sib, font));
        return text;
    }
}