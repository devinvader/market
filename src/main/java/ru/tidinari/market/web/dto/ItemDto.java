package ru.tidinari.market.web.dto;

public record ItemDto(
        long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {

  public static ItemDto empty() {
    return new ItemDto(-1, null, null, null, 0, 0);
  }
}
