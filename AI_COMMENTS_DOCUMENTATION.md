# AI Comments for Key Moves - Feature Documentation

## Overview

This feature automatically generates AI-powered comments for significant moves when exporting SGF files in Lizzie. The comments are generated using OpenAI's GPT API and are added to moves where the score difference exceeds a configurable threshold.

## Features

- **Automatic Detection**: Identifies key moves based on ScoreMean difference between consecutive moves
- **Russian Language**: Uses Russian prompt template as specified in requirements
- **Configurable Threshold**: Score difference threshold is configurable (default: 1.0 points)
- **API Integration**: Uses OpenAI GPT-3.5-turbo model for comment generation
- **Error Handling**: Graceful fallback when API is unavailable or incorrectly configured
- **Non-intrusive**: Preserves existing comment functionality and appends AI comments with [AI] prefix

## Configuration

### UI Settings (in Config Dialog)
1. **Enable AI comments for key moves** - Checkbox to enable/disable the feature
2. **OpenAI API Key** - Text field for your OpenAI API key
3. **Score difference threshold** - Numeric field for minimum score difference (default: 1.0)

### Configuration File
The settings are stored in `config.txt` under the `ui` section:
```json
{
  "ui": {
    "enable-ai-comments-for-key-moves": false,
    "openai-api-key": "",
    "ai-comments-score-threshold": 1.0
  }
}
```

## How It Works

1. **Move Analysis**: During SGF export, each move is analyzed for its impact on the game
2. **Threshold Check**: If the absolute ScoreMean difference between consecutive moves >= threshold
3. **Comment Generation**: An API call is made to OpenAI with the Russian prompt template
4. **Comment Integration**: The AI-generated comment is appended to existing move comments with "[AI]:" prefix

### Prompt Template
```
В партии Го после хода {moveDescription} преимущество изменилось на {scoreDelta} очков. 
Объясни, почему это важно и какой стратегический смысл у такого изменения.
```

### System Message
```
Ты эксперт по игре Го. Объясняй значимость ходов кратко и понятно, 
сосредотачиваясь на стратегических аспектах.
```

## Usage Instructions

1. **Get OpenAI API Key**: 
   - Visit https://platform.openai.com/api-keys
   - Create an API key with appropriate permissions

2. **Configure Lizzie**:
   - Open Settings/Configuration dialog
   - Enable "AI comments for key moves" checkbox
   - Enter your OpenAI API key
   - Set desired score threshold (optional, default 1.0)

3. **Export SGF**:
   - Play or analyze a game with AI analysis (KataGo/Leela Zero)
   - Export SGF file using File -> Save
   - AI comments will be automatically generated for significant moves

## Technical Implementation

### Files Modified

1. **`Config.java`**:
   - Added `enableAiCommentsForKeyMoves` boolean field
   - Added `openaiApiKey` string field
   - Added `aiCommentsScoreThreshold` double field
   - Updated default config creation and loading

2. **`OpenAIService.java`** (new):
   - Handles OpenAI API communication
   - Uses existing `AjaxHttpRequest` for HTTP calls
   - Implements error handling and validation

3. **`SGFParser.java`**:
   - Modified `generateNode()` to check for AI comment conditions
   - Added `shouldGenerateAiComment()` method for threshold checking
   - Added `generateAiComment()` method for comment generation
   - Integrates with existing comment system

4. **`ConfigDialog.java`**:
   - Added UI components for AI comments configuration
   - Implemented loading and saving of configuration values

### Error Handling

- **Empty/Invalid API Key**: Feature is disabled, no errors thrown
- **API Request Failures**: Logged to console, SGF export continues without AI comments
- **Network Issues**: Graceful degradation, original functionality preserved
- **Invalid Configuration**: Default values used, user notified in logs

## Dependencies

- **OpenAI API**: Requires valid OpenAI API key and internet connection
- **Existing HTTP Client**: Uses `featurecat.lizzie.util.AjaxHttpRequest`
- **JSON Library**: Uses existing `org.json` dependency

## Debugging

Enable console output to see debug information:
- AI comment trigger conditions
- Generated comments
- API errors and configuration issues

Example console output:
```
AI comment triggered: score delta 2.3 >= threshold 1.0
Generating AI comment for move: черные D4, score delta: 2.3
Generated AI comment: Этот ход создает важную базу в углу...
```

## Limitations

- Requires internet connection for OpenAI API calls
- API calls may slow down SGF export for games with many key moves
- Comments are in Russian only (as per requirements)
- Requires valid OpenAI API key and available credits

## Future Enhancements

- Caching of generated comments
- Support for multiple languages
- Alternative AI providers
- Batch processing for multiple moves
- Comment quality scoring and filtering