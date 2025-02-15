package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@UtilityClass
public class CommentMapper {
    public Comment toEntity(CommentDto dto, Item item, User user){
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        return comment;
    }

    public CommentDtoOut toDto(Comment comment) {
        return new CommentDtoOut(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated(),
                comment.getItem().getId()
        );
    }
}
