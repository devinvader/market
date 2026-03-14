package ru.devinvader.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.market.domain.Image;
import ru.devinvader.market.service.ImageService;
import ru.devinvader.market.web.controller.ImageController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageService imageService;

    private final Long ITEM_ID = 1L;
    private final Long IMAGE_ID = 100L;

    @Test
    void getImage_existingImage_returnsImageData() throws Exception {
        // given
        byte[] imageData = "изображение.png".getBytes();
        String contentType = "image/png";
        Image image = new Image(IMAGE_ID, imageData, contentType, null);
        when(imageService.getImageByItemId(ITEM_ID)).thenReturn(image);

        // when / then
        mockMvc.perform(get("/items/{id}/image", ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().bytes(imageData));
    }

    @Test
    void getImage_nonExistingImage_returnsNotFound() throws Exception {
        // given
        when(imageService.getImageByItemId(ITEM_ID)).thenReturn(null);

        // when / then
        mockMvc.perform(get("/items/{id}/image", ITEM_ID))
                .andExpect(status().isNotFound());
    }
}