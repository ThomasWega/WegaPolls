package me.wega.wegapolls.cmd.arg;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.wega.wegapolls.TimeString;

public final class GreedyTimeArgument extends CustomArgument<TimeString, String> {
    public GreedyTimeArgument(String nodeName) {
        super(new GreedyStringArgument(nodeName), info -> {
            try {
                return new TimeString(info.input());
            } catch (IllegalArgumentException e) {
                throw CustomArgumentException.fromString("Invalid time string: " + info.input());
            }
        });
        replaceSuggestions(ArgumentSuggestions.empty());
    }

}