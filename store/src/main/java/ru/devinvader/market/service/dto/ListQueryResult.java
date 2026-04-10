package ru.devinvader.market.service.dto;

import ru.devinvader.market.domain.Item;
import java.util.List;

public record ListQueryResult(List<Item> items, Long total) {}
