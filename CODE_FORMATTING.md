# Code Formatting Guide

Dự án này sử dụng **Spotless** với **ktlint** để đảm bảo code được format nhất quán.

## Cài đặt Git Hooks

### Lần đầu tiên clone dự án

Sau khi clone dự án, chạy lệnh sau để cài đặt Git hooks:

```bash
./install-hooks.sh
```

Script này sẽ tự động:
- Copy pre-commit hook vào `.git/hooks/`
- Set quyền thực thi cho hook
- Mỗi lần commit, code sẽ tự động được format

### Cách hoạt động

Khi bạn chạy `git commit`, pre-commit hook sẽ:
1. Tìm tất cả file Kotlin (.kt, .kts) đang được commit
2. Chạy `./gradlew spotlessApply` để format code
3. Tự động add lại các file đã được format
4. Tiếp tục commit

## Format thủ công

### Format toàn bộ dự án

```bash
./gradlew spotlessApply
```

### Kiểm tra format mà không thay đổi file

```bash
./gradlew spotlessCheck
```

## Quy tắc Format

Dự án sử dụng ktlint 1.0.1 với các quy tắc:

- **Indent**: 4 spaces
- **Max line length**: Disabled (không giới hạn)
- **Trailing whitespace**: Tự động xóa
- **Final newline**: Tự động thêm
- **Function naming**: Disabled (cho phép Composable functions bắt đầu bằng chữ hoa)
- **Wildcard imports**: Disabled (cho phép import *)

## Cấu hình

### build.gradle.kts

```kotlin
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint("1.0.1")
            .editorConfigOverride(
                mapOf(
                    "indent_size" to "4",
                    "max_line_length" to "off",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_standard_max-line-length" to "disabled",
                    "ktlint_standard_discouraged-comment-location" to "disabled"
                )
            )
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.0.1")
    }
}
```

### .editorconfig

File `.editorconfig` đã được tạo để IDE tự động format đúng chuẩn:

```
root = true

[*]
charset = utf-8
indent_size = 4
max_line_length = 120
insert_final_newline = true
trim_trailing_whitespace = true

[*.kt]
indent_size = 4
disabled_rules = no-wildcard-imports,import-ordering
```

## Cấu hình IDE

### Android Studio / IntelliJ IDEA

1. Mở **Settings/Preferences** → **Editor** → **Code Style** → **Kotlin**
2. Click **Set from...** → **Predefined Style** → **Kotlin style guide**
3. Trong tab **Tabs and Indents**: Set indent = 4
4. Trong tab **Imports**: Bỏ check **Use single name import**

Hoặc đơn giản hơn, IDE sẽ tự động đọc file `.editorconfig`.

## Bypass Pre-commit Hook

Trong trường hợp khẩn cấp (không khuyến khích), bạn có thể bỏ qua hook:

```bash
git commit --no-verify -m "message"
```

## Troubleshooting

### Hook không chạy

1. Kiểm tra quyền thực thi:
```bash
ls -la .git/hooks/pre-commit
```

2. Nếu không có quyền, chạy lại:
```bash
./install-hooks.sh
```

### Spotless báo lỗi

1. Chạy spotlessCheck để xem chi tiết:
```bash
./gradlew spotlessCheck
```

2. Sửa lỗi thủ công hoặc chạy:
```bash
./gradlew spotlessApply
```

### Windows

Nếu bạn dùng Windows, có thể cần chạy hook bằng Git Bash hoặc WSL.

## Tích hợp CI/CD

Thêm vào CI/CD pipeline để kiểm tra format:

```yaml
- name: Check code formatting
  run: ./gradlew spotlessCheck
```

---

**Lưu ý**: Luôn đảm bảo code được format đúng trước khi tạo Pull Request!
