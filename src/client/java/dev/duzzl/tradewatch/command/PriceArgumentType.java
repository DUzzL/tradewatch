package dev.duzzl.tradewatch.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Reads price:32 as one argument; Brigadier's word argument deliberately stops at ':'. */
public final class PriceArgumentType implements ArgumentType<String> {
    private static final PriceArgumentType INSTANCE = new PriceArgumentType();
    private PriceArgumentType() { }
    public static PriceArgumentType price() { return INSTANCE; }
    @Override public String parse(StringReader reader) { int start = reader.getCursor(); while (reader.canRead() && !Character.isWhitespace(reader.peek())) reader.skip(); return reader.getString().substring(start, reader.getCursor()); }
    @Override public Collection<String> getExamples() { return List.of("price:32"); }
    @Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) { for (int price = 1; price <= 64; price++) builder.suggest("price:" + price); return builder.buildFuture(); }
}
