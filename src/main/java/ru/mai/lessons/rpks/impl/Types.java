package ru.mai.lessons.rpks.impl;

public class Types {
    public record Grade(int subjectId, int studentId, int grade, String date) {
    }

    public record Group(int id, String name, int studentId) {
    }

    public record Student(int id, String name) {
    }

    public record Subject(int id, String name) {
    }
}
