package ru.devinvader.market.web.dto;

public record ItemDto(
        long id,
        String title,
        String description,
        long price,
        int count
) {

  public static ItemDto empty() {
    return new ItemDto(-1, "", "", 0, 0);
  }
}
