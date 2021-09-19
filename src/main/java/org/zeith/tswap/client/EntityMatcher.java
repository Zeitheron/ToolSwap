package org.zeith.tswap.client;

import net.minecraft.entity.Entity;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class EntityMatcher
		implements Predicate<Entity>
{
	public final Pattern pattern;

	public EntityMatcher(String typeInput)
	{
		this.pattern = Pattern.compile(wrapRegex(typeInput));
	}

	@Override
	public boolean test(Entity entity)
	{
		return entity != null && pattern.matcher(Objects.toString(entity.getType().getRegistryName())).matches();
	}

	public static String wrapRegex(String input)
	{
		StringBuilder sb = new StringBuilder();
		for(char c : input.toCharArray())
		{
			if(c == '?') sb.append(".?");
			else if(c == '*') sb.append(".*");
			else sb.append('[').append(c).append(']');
		}
		return sb.toString();
	}
}