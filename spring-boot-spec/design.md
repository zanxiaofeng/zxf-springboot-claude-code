# Design Document - Spring Boot 3 REST API Project Specification

## 1. Profile Baseline Declaration

- **Profile selection**: `profiles/general.md`
- **Selection rationale**: This presentation is a technical specification document for an internal development team, not a commercial report, academic defense, or marketing campaign. The general profile's "professional, clear, moderately polished" baseline fits a technical standards presentation perfectly.
- **Referenced dimensions**: Information density (medium-high), content-to-visual ratio (balanced), font hierarchy guidance, content expression techniques (tables, structured lists, SmartArt), decoration prohibitions.
- **Deviation notes**:
  - Color: Deviating from the generic "light colors preferred" guidance to use a refined dark-green primary with a near-white background, creating a subtle "Spring ecosystem" brand association without being cliché.
  - Layout: Increasing structure density slightly to accommodate code snippets and structured technical rules.
  - Decoration: Using a very restrained set of geometric accents to mimic the precision of engineering/IDE interfaces.

## 2. Style Baseline Declaration

- **Style anchor selection**:
  1. **Swiss International Style (Grid & Typography)**: Referenced for its rigorous grid alignment, clear typographic hierarchy, and objective presentation of information. We adopt the strict left-alignment and structured whitespace principles.
  2. **Modern Technical Documentation (GitHub/Atlassian/Stripe)**: Referenced for its clean readability, subtle use of color for code/semantics, and card-based separation of distinct logic blocks.
- **Referenced dimension explanation**:
  - From Swiss Style: Grid discipline, font size contrast, and the "form follows function" approach to layout.
  - From Technical Docs: The neutral background, highly readable body text, and the use of monospaced/label-like elements for code or file paths.

## 3. Style Details

### Color Design Principles
- **Overall tendency**: Conservative & steady with a high-tech professional feel. The presentation must convey authority, precision, and engineering discipline.
- **Temperature**: Cool-neutral, leaning towards a clean, digital, "editor-like" aesthetic.
- **Primary color**: `#065F46` (Deep Forest Green). Directly resonates with the Spring Boot ecosystem while maintaining a sophisticated, non-bright tone.
- **Background**: `#F8FAFC` (Very Light Cool Gray). Avoids sterile pure white, providing a softer canvas for long technical reading.
- **Text color**: `#1E293B` (Dark Slate). High contrast for readability.
- **Secondary color**: `#64748B` (Cool Gray). Used for dividers, secondary metadata, and subtle borders.
- **Accent color**: `#D97706` (Amber). Used extremely conservatively for critical warnings, error codes, or key "Prohibited" actions to draw attention without breaking the calm palette.
- **Light background**: `#ECFDF5` (Very Light Mint). Used exclusively as a background fill for code blocks or summary cards to separate them from the main background.

### Font Usage Principles
- **Title fonts**: `"Liter, MiSans"` — Liter provides a modern, rational neo-grotesque feel for English (perfect for tech), while MiSans ensures clean, modern legibility for Chinese.
- **Body fonts**: `"QuattrocentoSans, MiSans"` — QuattrocentoSans is classic, elegant, and highly readable at small sizes, ensuring dense technical text remains comfortable.
- **Font size hierarchy**:
  - Cover title: 48px (with expanded letter spacing for impact)
  - Chapter title: 40px
  - Page title: 28px
  - Subtitle/section header: 22px
  - Body text: 18px (minimum for dense technical content)
  - Footnotes/labels: 14px
- **Special treatments**: Page titles and section headers should use bold weight. Code snippets or file paths within text should use a slightly smaller size (16px) and a neutral color (#475569).

### Text Box and Container Styles
- **Content separation**: Primarily rely on whitespace and subtle 1px solid lines (`#E2E8F0`) for separation.
- **Cards**: Use sparingly. When used, they must be sharp-cornered (`rect`) with no border, filled with `#FFFFFF` or `#ECFDF5` to lift content off the main background.
- **Decorative elements**: 
  - A thin vertical accent bar (4px wide, `#065F46`) placed to the left of key summaries or rules.
  - Minimalist geometric shapes (rectangles) to group related API specifications or code logic.

### Image Style
- **Icons**: Solid icons (`fas`) in primary or secondary colors. Used functionally to label different specification categories (e.g., a shield for security, a code bracket for coding standards).
- **Tables**: Minimalist three-line style. Header row uses `primary` background with white text. Body rows alternate between white and `#F8FAFC`. Remove vertical borders to reduce clutter.
- **Charts**: Not heavily used, but if needed, use monochrome or analogous colors based on the primary green.
- **Illustrations**: Not applicable. This is a standards document. Visuals should be structural (SmartArt, flowcharts) rather than illustrative.

## 4. Layout System

### Global Layout Characteristics
- **Page size**: 1280x720 (16:9)
- **Page margins**: 60px left/right, 50px top, 40px bottom.
- **Unified elements**: 
  - A subtle page number at the bottom-right (14px, secondary color).
  - A thin horizontal line at the top or bottom of content pages to anchor the layout.
- **Grid alignment**: Strict left alignment for all text blocks. Elements in multi-column layouts must have perfectly aligned top and bottom baselines.

### Special Page Layouts
- **Cover**: 
  - Approach: "Hero + Geometric Frame".
  - A full-bleed dark abstract/tech background (`#0F172A`) with a gradient mask.
  - Centered or left-aligned large typography (White text) for the title. 
  - Subtitle and metadata (version, audience) placed cleanly below the title.
- **Table of Contents**: 
  - Approach: "Grid Cards".
  - Reject bullet points. Arrange chapters as 2x3 or 3x2 sharp-cornered cards on the light background.
  - Each card contains the chapter number (large, green) and chapter title.
- **Chapter transition**: 
  - A "reset" page using the primary green (`#065F46`) as the background.
  - Large, semi-transparent white typography for the chapter number.
  - Clean white chapter title.

### Content Page Layout Patterns
- **Pattern A (Top Title + 2-Column Grid)**: Title at the top, followed by a vertical divider splitting the page into two equal columns. Ideal for comparing "Do's and Don'ts" or "Rules vs. Examples".
- **Pattern B (Top Title + Structured Body)**: Title at the top, followed by full-width content blocks separated by horizontal lines or whitespace. Ideal for step-by-step TDD workflows or directory structures.
- **Pattern C (Left Bar + Content)**: A thick vertical accent bar on the left, with the main content indented to the right. Ideal for highlighting key rules or mandatory prohibitions.
- **Pattern D (Full-width Table/SmartArt)**: Title at the top, with a full-width table or geometric SmartArt (built with shapes) occupying the lower 75% of the page. Ideal for HTTP method semantics or CI/CD pipeline steps.

## 5. Style Usage Rules

- **$title**: Used exclusively for Cover title and Chapter transition titles.
- **$heading**: Used for page titles on all content pages.
- **$subheading**: Used for section headers within a page (e.g., "Naming Rules", "Injection Rules").
- **$body**: Used for all standard descriptive text, bullet points, and explanations.
- **$caption**: Used for file paths, code snippets, page numbers, and data source annotations.
- **$primary**: Applied to important labels, icon fills, table headers, and the left accent bar.
- **$secondary**: Applied to secondary text, subtle borders, and inactive elements.
- **$accent**: Applied ONLY to critical warning text or "Prohibited" markers to ensure they stand out.
- **$lightBg**: Applied as the background fill for code snippet boxes or summary cards.

## 6. Risk Prohibitions

- [ ] **Color**: Do NOT use bright Spring Green (`#6DB33F`) as the primary color; it looks amateurish. Stick to the deep forest green `#065F46`.
- [ ] **Color**: Do NOT use Red/Green/Yellow to represent status unless absolutely necessary (the document specifies error codes, but do not use generic traffic-light colors).
- [ ] **Layout**: Do NOT leave large blank spaces on content pages. Technical specifications demand high information density (65-80%).
- [ ] **Layout**: Do NOT create misaligned columns. In left-right layouts, ensure both sides visually balance (top and bottom alignment).
- [ ] **Decoration**: Do NOT use rounded rectangles. Sharp corners (`rect`) maintain the technical, precise aesthetic.
- [ ] **Decoration**: Do NOT use gradients for shape fills (except for the cover background mask). Solid colors only.
- [ ] **Font Size**: Body text must NOT go below 18px. Footnotes/captions must NOT go below 14px.
- [ ] **Content**: Do NOT simply paste walls of code. Distill code into structural SmartArt or highlight only the critical lines within a bordered box.

## 7. Theme Definition

```yaml
theme:
  colors:
    primary: "#065F46"
    secondary: "#64748B"
    accent: "#D97706"
    background: "#F8FAFC"
    text: "#1E293B"
    lightBg: "#ECFDF5"
    dark: "#0F172A"
    white: "#FFFFFF"
  textStyles:
    title:
      fontSize: 48
      color: "$white"
      fontFamily: "Liter, MiSans"
      letterSpacing: 2
      lineHeight: 1.2
    heading:
      fontSize: 28
      color: "$text"
      fontFamily: "Liter, MiSans"
      lineHeight: 1.3
    subheading:
      fontSize: 22
      color: "$primary"
      fontFamily: "Liter, MiSans"
      lineHeight: 1.4
    body:
      fontSize: 18
      color: "$text"
      fontFamily: "QuattrocentoSans, MiSans"
      lineHeight: 1.6
    caption:
      fontSize: 14
      color: "$secondary"
      fontFamily: "QuattrocentoSans, MiSans"
      lineHeight: 1.4
  tableStyles:
    default:
      fontSize: 16
      fontFamily: "QuattrocentoSans, MiSans"
      headerFill: "$primary"
      headerColor: "$white"
      headerBold: true
      bodyFill: ["$white", "#F1F5F9"]
      bodyColor: "$text"
      border:
        style: solid
        width: 1
        color: "#E2E8F0"
```