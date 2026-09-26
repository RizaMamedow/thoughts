package dev.thoughts.app.ui.features.thoughts

import android.content.ClipData
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import dev.thoughts.app.R
import dev.thoughts.app.data.TagParser
import dev.thoughts.app.data.ThoughtWithTags
import dev.thoughts.app.ui.features.thoughts.components.AboutDialog
import dev.thoughts.app.ui.features.thoughts.components.AppDrawerContent
import dev.thoughts.app.ui.features.thoughts.components.BulkTagDialog
import dev.thoughts.app.ui.features.thoughts.components.DayHeader
import dev.thoughts.app.ui.features.thoughts.components.EditDialog
import dev.thoughts.app.ui.features.thoughts.components.EmptyState
import dev.thoughts.app.ui.features.thoughts.components.QuickInput
import dev.thoughts.app.ui.features.thoughts.components.SearchField
import dev.thoughts.app.ui.features.thoughts.components.SelectionTopBar
import dev.thoughts.app.ui.features.thoughts.components.SwipeToDelete
import dev.thoughts.app.ui.features.thoughts.components.ThoughtCard
import kotlinx.coroutines.launch

class ThoughtsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val vm = koinScreenModel<ThoughtsScreenModel>()
        val state by vm.state.collectAsStateWithLifecycle()
        val themeMode by vm.themeMode.collectAsStateWithLifecycle()
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val clipboard = LocalClipboard.current
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        var searchOpen by rememberSaveable { mutableStateOf(false) }
        var editing by remember { mutableStateOf<ThoughtWithTags?>(null) }
        var taggingSelection by remember { mutableStateOf(false) }
        var showAbout by remember { mutableStateOf(false) }

        val selectedItems = remember(state.days, state.selectedIds) {
            state.days.flatMap { it.thoughts }.filter { it.thought.id in state.selectedIds }
        }

        LaunchedEffect(state.isSelectionMode) {
            if (state.isSelectionMode) keyboardController?.hide()
        }
        LaunchedEffect(drawerState.targetValue) {
            if (drawerState.targetValue == DrawerValue.Open) keyboardController?.hide()
        }

        BackHandler(enabled = state.isSelectionMode) { vm.clearSelection() }
        BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

        val appName = stringResource(R.string.app_name)
        val copiedMessage = stringResource(R.string.copied_count, selectedItems.size)
        val undoLabel = stringResource(R.string.undo)
        val thoughtDeletedMessage = stringResource(R.string.thought_deleted)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    themeMode = themeMode,
                    onThemeModeChange = vm::setThemeMode,
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        showAbout = true
                    },
                )
            },
        ) {
            Scaffold(
                topBar = {
                    if (state.isSelectionMode) {
                        SelectionTopBar(
                            count = state.selectedIds.size,
                            onClose = vm::clearSelection,
                            onCopy = {
                                val text = selectedItems.joinToString("\n\n") {
                                    TagParser.join(it.thought.text, it.tags)
                                }
                                vm.clearSelection()
                                scope.launch {
                                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(appName, text)))
                                    snackbar.currentSnackbarData?.dismiss()
                                    snackbar.showSnackbar(copiedMessage)
                                }
                            },
                            onTag = { taggingSelection = true },
                            onDelete = {
                                val items = selectedItems
                                val deletedMessage = context.getString(R.string.deleted_count, items.size)
                                vm.deleteSelected(state.selectedIds)
                                vm.clearSelection()
                                scope.launch {
                                    snackbar.currentSnackbarData?.dismiss()
                                    val result = snackbar.showSnackbar(
                                        message = deletedMessage,
                                        actionLabel = undoLabel,
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) vm.restoreAll(items)
                                }
                            },
                        )
                    } else {
                        TopAppBar(
                            title = {
                                if (searchOpen) {
                                    SearchField(state.query, vm::setQuery)
                                } else {
                                    Text(appName)
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_description))
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    if (searchOpen) vm.setQuery("")
                                    searchOpen = !searchOpen
                                }) {
                                    Icon(
                                        if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = stringResource(R.string.search_description),
                                    )
                                }
                            },
                        )
                    }
                },
                bottomBar = {
                    val hint = state.selectedTag?.let { stringResource(R.string.quick_input_hint_tag, it) }
                        ?: stringResource(R.string.quick_input_hint_default)
                    QuickInput(hint = hint, onSend = vm::add)
                },
                snackbarHost = { SnackbarHost(snackbar) },
            ) { padding ->
                Column(Modifier.padding(padding).fillMaxSize()) {
                    if (state.tags.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.tags, key = { it.tag }) { tc ->
                                FilterChip(
                                    selected = tc.tag == state.selectedTag,
                                    onClick = { vm.selectTag(tc.tag) },
                                    label = { Text(stringResource(R.string.tag_chip_format, tc.tag, tc.count)) },
                                )
                            }
                        }
                    }

                    if (state.days.isEmpty()) {
                        EmptyState(
                            text = if (state.isEmpty) {
                                stringResource(R.string.empty_state_no_thoughts)
                            } else {
                                stringResource(R.string.empty_state_no_results)
                            },
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            state.days.forEach { day ->
                                stickyHeader(key = day.date.toEpochDay()) { DayHeader(day.date) }
                                items(day.thoughts, key = { it.thought.id }) { item ->
                                    val isSelected = item.thought.id in state.selectedIds
                                    if (state.isSelectionMode) {
                                        ThoughtCard(
                                            item = item,
                                            selectedTag = state.selectedTag,
                                            selected = isSelected,
                                            onClick = { vm.toggleSelection(item.thought.id) },
                                            onLongClick = { vm.toggleSelection(item.thought.id) },
                                            onTagClick = vm::selectTag,
                                            modifier = Modifier.animateItem(),
                                        )
                                    } else {
                                        SwipeToDelete(
                                            modifier = Modifier.animateItem(),
                                            onDelete = {
                                                vm.delete(item)
                                                scope.launch {
                                                    snackbar.currentSnackbarData?.dismiss()
                                                    val result = snackbar.showSnackbar(
                                                        message = thoughtDeletedMessage,
                                                        actionLabel = undoLabel,
                                                        duration = SnackbarDuration.Short,
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) vm.restore(item)
                                                }
                                            },
                                        ) {
                                            ThoughtCard(
                                                item = item,
                                                selectedTag = state.selectedTag,
                                                selected = false,
                                                onClick = { editing = item },
                                                onLongClick = { vm.toggleSelection(item.thought.id) },
                                                onTagClick = vm::selectTag,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            editing?.let { item ->
                EditDialog(
                    item = item,
                    onDismiss = { editing = null },
                    onSave = {
                        vm.edit(item.thought.id, it)
                        editing = null
                    },
                )
            }

            if (taggingSelection) {
                val ids = state.selectedIds
                BulkTagDialog(
                    onDismiss = { taggingSelection = false },
                    onAdd = { tag ->
                        vm.addTagToSelected(ids, tag)
                        vm.clearSelection()
                        taggingSelection = false
                    },
                    onRemove = { tag ->
                        vm.removeTagFromSelected(ids, tag)
                        vm.clearSelection()
                        taggingSelection = false
                    },
                )
            }

            if (showAbout) {
                AboutDialog(onDismiss = { showAbout = false })
            }
        }
    }
}
