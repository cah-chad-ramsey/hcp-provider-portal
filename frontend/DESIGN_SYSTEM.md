# SONXTRA Design System Implementation

This document describes the SONXTRA design system implementation in the HCP Provider Portal frontend.

## Design System Source

The design system is based on the specifications extracted from `cp360.pen`, which contains the complete SONXTRA brand identity, color palette, typography, and component library.

## Implementation Overview

### 1. Custom Angular Material Theme (`theme.scss`)

**Location:** `frontend/src/theme.scss`

The custom theme implements:
- Material 3 theme with SONXTRA brand colors
- Cardinal Red (#C41030) as primary color
- Sonexus Teal (#008E97) as accent color
- Inter font family throughout all components
- Custom component sizing and styling overrides

### 2. Global Styles (`styles.scss`)

**Location:** `frontend/src/styles.scss`

Implements comprehensive SONXTRA design tokens:

#### Color Palette
- **Primary:** Cardinal Red (#C41030)
- **Secondary:** Sonexus Teal (#008E97)
- **Neutrals:** Gray scale from #111827 to #FFFFFF
- **Backgrounds:** Light grays (#F9FAFB, #F3F4F6)

#### Typography Scale
- **Font Family:** Inter (400, 600, 700, 800 weights)
- **H1:** 32px/40px, Bold (700)
- **H2:** 24px/32px, Bold (700)
- **H3:** 18px/28px, Semibold (600)
- **Body:** 16px/24px, Normal (400)
- **Body Small:** 14px/20px, Normal (400)
- **Caption:** 12px/16px, Normal (400)

#### Spacing System
Based on 4px grid:
- Compact: 4px
- Small: 8px
- Medium: 12px
- Base: 16px
- Large: 24px
- XL: 32px
- 2XL: 40px
- 3XL: 48px

#### Component Sizing
- **Button Height:** 44px
- **Input Height:** 40px
- **Header Height:** 64px
- **Sidebar Width:** 260px
- **Border Radius Small:** 4px
- **Border Radius Medium:** 8px

### 3. SONXTRA Header Component

**Location:** `frontend/src/app/shared/components/app-header/`

Exact implementation of the AppHeader from cp360.pen:

#### Logo (Brand/SonextraLogo)
- Lightning bolt icon (Cardinal Red #C41030)
- "SONXTRA" text (18px, Extra Bold 800, Neutral Gray #374151)
- "SUPPORT" text (18px, Extra Bold 800, Neutral Gray #374151)
- 4px gap between elements
- Height: 24px icon container

#### Header Layout
- Height: 64px
- Background: White (#FFFFFF)
- Border bottom: 1px solid #E5E7EB
- Padding: 0px 48px
- Max width: 1440px
- Horizontal layout with space-between alignment

#### Navigation Links
- Font: Inter, 12px, Bold (700)
- Color: Cardinal Red (#C41030)
- Links: "Important Safety Information", "Prescribing Information", "Medication Guide"
- 24px gap between items

#### User Menu
- Displays current user name
- Dropdown with navigation options
- Logout functionality
- Hover states with gray background

### 4. Sidebar Navigation Component

**Location:** `frontend/src/app/shared/components/app-sidebar/`

Implementation of sidebar navigation pattern from cp360.pen:

#### Layout
- Width: 260px
- Background: White (#FFFFFF)
- Border right: 1px solid #E5E7EB
- Padding: 24px vertical

#### Navigation Items
- Font: Inter, 14px, Semibold (600)
- Inactive: #4B5563 (Nav Link Gray)
- Active: #008E97 (Teal) with light background
- Active indicator: 3px left border in Teal
- Padding: 12px 24px
- Hover state: Light gray background

#### Header
- Title: "Navigation"
- Font: Inter, 18px, Bold (700)
- Color: Cardinal Red (#C41030)

### 5. Main Layout Component

**Location:** `frontend/src/app/shared/components/main-layout/`

Combines header and sidebar into unified layout:

#### Structure
```
┌─────────────────────────────────────────┐
│          SONXTRA Header (64px)          │
├──────────┬──────────────────────────────┤
│          │                              │
│ Sidebar  │      Main Content            │
│ (260px)  │      (Flex 1)                │
│          │                              │
└──────────┴──────────────────────────────┘
```

#### Features
- Sticky header at top
- Full-height sidebar
- Scrollable main content area
- Gray background (#F9FAFB)
- 32px padding in content area
- Responsive: stacks vertically on mobile

### 6. Custom Button Classes

Three button variants matching cp360.pen specifications:

#### Primary Button (`.btn-primary`)
- Background: Cardinal Red (#C41030)
- Text: White (#FFFFFF)
- Height: 44px
- Border radius: 4px
- Padding: 10px 24px
- Font: Inter, 14px, Semibold (600)
- Hover: Darker red (#9f0c26)

#### Secondary Button (`.btn-secondary`)
- Background: Sonexus Teal (#008E97)
- Text: White (#FFFFFF)
- Same sizing as primary
- Hover: Darker teal (#007b84)

#### Outline Button (`.btn-outline`)
- Background: White (#FFFFFF)
- Text: Neutral Gray (#374151)
- Border: 1px solid #E5E7EB
- Same sizing as primary
- Hover: Light gray background

### 7. Form Components

Custom form field styling:

#### Text Inputs
- Height: 40px
- Padding: 10px 12px
- Border: 1px solid #D1D5DB (Gray 300)
- Border radius: 4px
- Font: Inter, 14px, Normal
- Placeholder: #9CA3AF (Gray 400)
- Focus: Teal border with box-shadow

#### Labels
- Font: Inter, 14px, Semibold (600)
- Color: #374151 (Neutral Gray)
- 8px gap from input

#### Checkboxes
- Size: 18px × 18px
- Border radius: 4px
- Border: 1px solid #D1D5DB

### 8. Card Components

#### Standard Card (`.card`)
- Background: White (#FFFFFF)
- Border: 1px solid #E5E7EB
- Border radius: 8px
- Padding: 24px
- No shadow (flat design)

#### Action Card (`.card-action`)
- Width: 300px
- Vertical flex layout
- 16px gap between elements

### 9. Utility Classes

#### Layout Utilities
- `.flex`, `.flex-row`, `.flex-column`
- `.items-center`, `.justify-between`, `.justify-center`
- `.gap-small`, `.gap-medium`, `.gap-base`, `.gap-large`

#### Content Containers
- `.container`: 1440px max-width with 48px padding
- `.page-content`: Main content area with 32px padding
- `.content-section`: White card for content blocks

#### Typography Helpers
- `.text-center`, `.text-right`
- `.body-small`, `.caption`, `.label`

#### Badges
- `.badge-primary`: Cardinal Red
- `.badge-success`: Teal
- `.badge-warning`: Orange
- `.badge-error`: Red
- `.badge-neutral`: Gray

### 10. Data Tables

Styling for tabular data:

- Full width border-collapse
- Header: Gray background (#F3F4F6)
- Borders: 1px solid #E5E7EB
- Padding: 12px
- Hover: Light gray background on rows
- Font: Inter, 14px

## Using the Design System

### In Components

1. **Use Custom Button Classes:**
```html
<button class="btn-primary">Submit</button>
<button class="btn-secondary">Cancel</button>
<button class="btn-outline">Back</button>
```

2. **Use Angular Material with SONXTRA Theme:**
```html
<button mat-raised-button color="primary">Primary</button>
<button mat-raised-button color="accent">Accent</button>
```

3. **Use Form Fields:**
```html
<div class="form-field">
  <label>Email</label>
  <input type="email" placeholder="Enter email">
</div>
```

4. **Use Layout Utilities:**
```html
<div class="flex items-center justify-between gap-medium">
  <h2>Title</h2>
  <button class="btn-primary">Action</button>
</div>
```

5. **Use Cards:**
```html
<div class="card">
  <h3>Card Title</h3>
  <p>Card content...</p>
</div>
```

### CSS Variables

All design tokens are available as CSS variables:

```css
.custom-component {
  background-color: var(--color-white);
  border: var(--border);
  border-radius: var(--radius-medium);
  padding: var(--spacing-large);
  color: var(--color-neutral-gray);
  font-family: 'Inter', sans-serif;
}
```

## Design System Files

| File | Purpose |
|------|---------|
| `frontend/src/theme.scss` | Angular Material custom theme |
| `frontend/src/styles.scss` | Global styles, utilities, components |
| `frontend/src/app/shared/components/app-header/` | SONXTRA branded header |
| `frontend/src/app/shared/components/app-sidebar/` | Navigation sidebar |
| `frontend/src/app/shared/components/main-layout/` | Layout wrapper |

## Browser Support

- Modern evergreen browsers (Chrome, Firefox, Safari, Edge)
- CSS Grid and Flexbox required
- CSS Custom Properties (variables) required
- Inter font loaded from Google Fonts

## Mobile Responsiveness

- Header: Navigation links collapse on mobile (<768px)
- Sidebar: Converts to horizontal tabs on mobile
- Layout: Stacks vertically on mobile
- Content padding: Reduces to 16px on mobile

## Accessibility

- Sufficient color contrast (WCAG AA)
- Inter font family for readability
- Clear visual hierarchy with typography scale
- Focus states on interactive elements
- Semantic HTML structure

## Future Enhancements

- Dark mode support (add dark theme variant)
- Additional component variants (chips, tooltips, etc.)
- Animation and transition system
- Icon library integration
- More spacing utilities

## References

- Source Design: `cp360.pen`
- Angular Material: https://material.angular.io/
- Inter Font: https://fonts.google.com/specimen/Inter
- Design System Specification: See cp360.pen analysis in project documentation

---

**Last Updated:** February 2026
**Design System Version:** 1.0.0
**Based On:** cp360.pen SONXTRA Design System
