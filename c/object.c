#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "object.h"
#include "value.h"
#include "vm.h"

static ObjString* allocate_string(char* chars, int length);
static Obj* allocate_object(size_t size, ObjType type);

#define ALLOCATE_OBJ(type, objectType) \
    (type*)allocate_object(sizeof(type), objectType)

ObjString* take_string(char* chars, int length) {
	return allocate_string(chars, length);
}

ObjString* copy_string(const char* chars, int length) {
	char* heapChars = ALLOCATE(char, length + 1);
	memcpy(heapChars, chars, length);
	heapChars[length] = '\0';
	return allocate_string(heapChars, length);
}

void print_object(Value value) {
	switch (OBJ_TYPE(value)) {
	case OBJ_STRING:
		printf("%s", AS_CSTRING(value));
		break;
	}
}

static ObjString* allocate_string(char* chars, int length) {
	ObjString* string = ALLOCATE_OBJ(ObjString, OBJ_STRING);
	string->length = length;
	string->chars = chars;
	return string;
}

static Obj* allocate_object(size_t size, ObjType type) {
	Obj* object = (Obj*)reallocate(NULL, 0, size);
	object->type = type;

	object->next = vm.objects;
	vm.objects = object;

	return object;
}


