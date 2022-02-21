package net.darkhax.bookshelf.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.darkhax.bookshelf.api.registry.ICommandBuilder;
import net.darkhax.bookshelf.api.util.TextHelper;
import net.darkhax.bookshelf.impl.commands.args.FontArgument;
import net.darkhax.bookshelf.mixin.block.entity.AccessorSignBlockEntity;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class BookshelfCommands implements ICommandBuilder {

    @Override
    public void build(CommandDispatcher<CommandSourceStack> dispatcher, boolean isDedicated) {

        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("bookshelf");
        this.buildFontCommands(root, isDedicated);

        dispatcher.register(root);
    }

    private void buildFontCommands(LiteralArgumentBuilder<CommandSourceStack> root, boolean isDedicated) {

        final LiteralArgumentBuilder<CommandSourceStack> font = Commands.literal("font");

        font.then(Commands.literal("item").requires(p -> p.hasPermission(2)).then(FontArgument.argument().executes(this::renameItemWithFont)));
        font.then(Commands.literal("block").requires(p -> p.hasPermission(2)).then(FontArgument.argument().then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(this::renameBlockWithFont))));
        font.then(Commands.literal("book").requires(p -> p.hasPermission(2)).then(FontArgument.argument().executes(this::setBookFont)));
        font.then(Commands.literal("say").requires(p -> p.hasPermission(2)).then(FontArgument.argument().then(Commands.argument("message", MessageArgument.message()).executes(this::speakWithFont))));
        root.then(font);
    }

    private int speakWithFont(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        final ResourceLocation fontId = FontArgument.getFont(context);
        final Component inputMessage = TextHelper.applyFont(MessageArgument.getMessage(context, "message"), fontId);
        final Component txtMessage = new TranslatableComponent("chat.type.announcement", context.getSource().getDisplayName(), inputMessage);
        final Entity sender = context.getSource().getEntity();

        if (sender != null) {

            context.getSource().getServer().getPlayerList().broadcastMessage(txtMessage, ChatType.CHAT, sender.getUUID());
        }

        else {

            context.getSource().getServer().getPlayerList().broadcastMessage(txtMessage, ChatType.SYSTEM, Util.NIL_UUID);
        }

        return 1;
    }

    private int renameItemWithFont(CommandContext<CommandSourceStack> context) {

        final ResourceLocation fontId = FontArgument.getFont(context);
        final Entity sender = context.getSource().getEntity();

        if (sender instanceof LivingEntity living) {

            final ItemStack stack = living.getMainHandItem();
            stack.setHoverName(TextHelper.applyFont(stack.getHoverName(), fontId));
            return 1;
        }

        return 0;
    }

    private int renameBlockWithFont(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        final ServerLevel world = context.getSource().getLevel();
        final ResourceLocation fontId = FontArgument.getFont(context);
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        final BlockEntity tile = world.getBlockEntity(pos);

        if (tile != null) {

            if (tile instanceof BaseContainerBlockEntity container) {

                container.setCustomName(TextHelper.applyFont(container.getName(), fontId));
            }

            if (tile instanceof SignBlockEntity sign) {

                for (int i = 0; i < 4; i++) {

                    final Component component = sign.getMessage(i, false);

                    if (component != null && component != Component.EMPTY) {

                        sign.setMessage(i, TextHelper.applyFont(component.copy(), fontId));
                    }
                }

                ((AccessorSignBlockEntity) sign).bookshelf$markUpdated();
            }
        }

        return 1;
    }

    private int setBookFont(CommandContext<CommandSourceStack> context) {

        final ResourceLocation fontId = FontArgument.getFont(context);
        final Entity sender = context.getSource().getEntity();

        if (sender instanceof LivingEntity living) {

            final ItemStack stack = living.getMainHandItem();

            if (stack.getItem() instanceof WrittenBookItem book && stack.hasTag()) {

                stack.setHoverName(TextHelper.applyFont(stack.getHoverName(), fontId));

                final CompoundTag stackTag = stack.getTag();

                if (stackTag != null) {

                    final ListTag pageData = stackTag.getList("pages", Tag.TAG_STRING);

                    for (int pageNum = 0; pageNum < pageData.size(); pageNum++) {

                        final Component pageText = Component.Serializer.fromJsonLenient(pageData.getString(pageNum));
                        TextHelper.applyFont(pageText, fontId);
                        pageData.set(pageNum, StringTag.valueOf(Component.Serializer.toJson(pageText)));
                    }

                    stackTag.put("pages", pageData);
                }

                return 1;
            }
        }

        return 0;
    }
}